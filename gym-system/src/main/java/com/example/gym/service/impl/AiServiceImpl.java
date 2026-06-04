package com.example.gym.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.gym.common.exception.BusinessException;
import com.example.gym.common.exception.ErrorCode;
import com.example.gym.config.DeepSeekConfig;
import com.example.gym.entity.AiChatMessage;
import com.example.gym.entity.AiChatSession;
import com.example.gym.entity.GymCourse;
import com.example.gym.entity.SysUser;
import com.example.gym.mapper.AiChatMessageMapper;
import com.example.gym.mapper.AiChatSessionMapper;
import com.example.gym.mapper.BookingMapper;
import com.example.gym.mapper.CourseMapper;
import com.example.gym.mapper.UserMapper;
import com.example.gym.service.AiService;
import com.example.gym.vo.AiChatMessageVO;
import com.example.gym.vo.AiChatSessionVO;
import com.example.gym.vo.BookingVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * AI 客服业务实现，实现 AiService 接口，对接 DeepSeek API。
 * <p>
 * 提供同步和 SSE 流式两种调用方式。核心机制：
 * <ul>
 *   <li>每次请求拼接 System Prompt（含用户实时信息：余额、VIP、可预约课程、历史预约）</li>
 *   <li>携带最近 {@link #HISTORY_LIMIT} 条历史消息，控制 token 消耗</li>
 *   <li>同步模式等完整回复后一次性返回</li>
 *   <li>流式模式在新线程中逐 chunk 推送，支持客户端断连时保存已接收内容</li>
 * </ul>
 * 会话管理支持新建、列表查询（带最后消息预览）、消息查询、删除。
 * <p>
 * API Key 优先读环境变量 DEEPSEEK_API_KEY，未设置则回退到 application.yml 配置。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    // 每次请求携带的最近历史消息条数（user + assistant 合计），控制 token 消耗
    private static final int HISTORY_LIMIT = 20;

    private final UserMapper userMapper;
    private final CourseMapper courseMapper;
    private final BookingMapper bookingMapper;
    private final AiChatSessionMapper sessionMapper;
    private final AiChatMessageMapper messageMapper;
    private final DeepSeekConfig deepSeekConfig;

    // System Prompt 模板，%s 位置由 getContextPrompt() 填入当前用户的实时信息
    private static final String SYSTEM_PROMPT_TEMPLATE =
            "你是「FitLife 健身房」的 AI 智能客服，名叫小健。\n\n"
            + "【你的能力范围】\n"
            + "- 介绍和推荐课程（根据用户余额、VIP折扣、个人偏好）\n"
            + "- 解答关于课程、教练、时间、价格、VIP权益的问题\n"
            + "- 帮用户规划健身计划（根据目标推荐课程组合）\n"
            + "- 解释如何使用系统功能（预约/取消/支付均需在对应页面自行操作）\n\n"
            + "【你不能做的事】\n"
            + "- 代替用户进行任何操作（预约/取消/支付）\n"
            + "- 提供医疗或专业健康诊断建议\n"
            + "- 回答与健身房无关的话题（礼貌拒绝并引导回正题）\n\n"
            + "【当前用户信息】\n"
            + "%s\n"
            + "【回答规则】\n"
            + "1. 用口语化、亲切的中文，像专业健身顾问而非客服机器人\n"
            + "2. 回答控制在200字以内，需列举时用简短列表\n"
            + "3. 不确定的内容说「需联系前台确认」，不猜测或编造\n"
            + "4. 如用户要预约/取消，说明需在「预约课程」或「我的预约」页面自行操作";

    // ======================== 公开接口 ========================

    @Override
    public AiChatMessageVO chat(Long userId, Long sessionId, String message) {
        if (StrUtil.isBlank(message)) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "消息不能为空");
        }
        if (!deepSeekConfig.isEnabled()) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "AI 功能未开启，请联系管理员");
        }

        AiChatSession session = getOrCreateSession(userId, sessionId, message);

        AiChatMessage userMsg = new AiChatMessage();
        userMsg.setSessionId(session.getId());
        userMsg.setRole("user");
        userMsg.setContent(message);
        userMsg.setCreateTime(LocalDateTime.now());
        messageMapper.insert(userMsg);

        String context = getContextPrompt(userId);
        String reply;
        try {
            reply = callDeepSeekSync(session.getId(), context);
        } catch (Exception e) {
            log.error("DeepSeek API 调用失败: {}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 服务暂时不可用，请稍后重试");
        }

        AiChatMessage assistantMsg = new AiChatMessage();
        assistantMsg.setSessionId(session.getId());
        assistantMsg.setRole("assistant");
        assistantMsg.setContent(reply);
        assistantMsg.setCreateTime(LocalDateTime.now());
        messageMapper.insert(assistantMsg);

        session.setUpdateTime(LocalDateTime.now());
        sessionMapper.updateById(session);

        return AiChatMessageVO.builder()
                .id(assistantMsg.getId())
                .sessionId(session.getId())
                .role("assistant")
                .content(reply)
                .createTime(assistantMsg.getCreateTime())
                .build();
    }

    /**
     * SSE 流式推送：在新线程中调用 DeepSeek stream 接口，每收到一个 chunk 立即推给前端。
     * 首帧先发 sessionId（新建会话时前端需要用它做后续请求），末帧发 [DONE] 通知前端结束。
     * 客户端断连时捕获 ClientDisconnectedException，保存已收到的部分回复后安静退出。
     */
    @Override
    public void chatStream(Long userId, Long sessionId, String message, SseEmitter emitter) {
        if (StrUtil.isBlank(message)) {
            emitter.completeWithError(new BusinessException(ErrorCode.PARAM_INVALID, "消息不能为空"));
            return;
        }
        if (!deepSeekConfig.isEnabled()) {
            emitter.completeWithError(new BusinessException(ErrorCode.PARAM_INVALID, "AI 功能未开启"));
            return;
        }

        AiChatSession session = getOrCreateSession(userId, sessionId, message);

        AiChatMessage userMsg = new AiChatMessage();
        userMsg.setSessionId(session.getId());
        userMsg.setRole("user");
        userMsg.setContent(message);
        userMsg.setCreateTime(LocalDateTime.now());
        messageMapper.insert(userMsg);

        String context = getContextPrompt(userId);
        final Long fSessionId = session.getId();

        new Thread(() -> {
            StringBuilder fullReply = new StringBuilder();
            try {
                // 首帧传 sessionId，让前端（新会话场景）得知服务端分配的 sessionId
                emitter.send(SseEmitter.event()
                        .data("{\"type\":\"session\",\"sessionId\":" + fSessionId + "}"));

                callDeepSeekStream(fSessionId, context, chunk -> {
                    fullReply.append(chunk);
                    try {
                        emitter.send(SseEmitter.event().data(chunk));
                    } catch (IOException e) {
                        throw new ClientDisconnectedException();
                    }
                });

                saveAssistantMessage(fSessionId, fullReply.toString());
                emitter.send(SseEmitter.event().data("[DONE]"));
                emitter.complete();

            } catch (ClientDisconnectedException e) {
                log.info("客户端已断开，会话 {}", fSessionId);
                // 保存已推送的部分内容，避免对话记录丢失
                if (!fullReply.isEmpty()) {
                    saveAssistantMessage(fSessionId, fullReply.toString());
                }
            } catch (Exception e) {
                log.error("流式推送异常，会话 {}: {}", fSessionId, e.getMessage());
                try {
                    emitter.send(SseEmitter.event().data("[ERROR]"));
                    emitter.complete();
                } catch (Exception ignored) {}
            }
        }).start();
    }

    @Override
    public List<AiChatSessionVO> getSessions(Long userId) {
        List<AiChatSession> sessions = sessionMapper.selectList(
                new LambdaQueryWrapper<AiChatSession>()
                        .eq(AiChatSession::getUserId, userId)
                        .orderByDesc(AiChatSession::getUpdateTime));

        return sessions.stream().map(session -> {
            AiChatMessage lastMsg = messageMapper.selectOne(
                    new LambdaQueryWrapper<AiChatMessage>()
                            .eq(AiChatMessage::getSessionId, session.getId())
                            .orderByDesc(AiChatMessage::getCreateTime)
                            .last("LIMIT 1"));
            String preview = lastMsg != null
                    ? (lastMsg.getContent().length() > 30
                            ? lastMsg.getContent().substring(0, 30) + "…"
                            : lastMsg.getContent())
                    : "";
            return AiChatSessionVO.builder()
                    .id(session.getId())
                    .title(session.getTitle())
                    .lastMessage(preview)
                    .updateTime(session.getUpdateTime())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public List<AiChatMessageVO> getMessages(Long userId, Long sessionId) {
        AiChatSession session = sessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "会话不存在");
        }
        List<AiChatMessage> messages = messageMapper.selectList(
                new LambdaQueryWrapper<AiChatMessage>()
                        .eq(AiChatMessage::getSessionId, sessionId)
                        .orderByAsc(AiChatMessage::getCreateTime));
        return messages.stream().map(m -> AiChatMessageVO.builder()
                .id(m.getId())
                .sessionId(m.getSessionId())
                .role(m.getRole())
                .content(m.getContent())
                .createTime(m.getCreateTime())
                .build()).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteSession(Long userId, Long sessionId) {
        AiChatSession session = sessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "会话不存在");
        }
        messageMapper.delete(new LambdaQueryWrapper<AiChatMessage>()
                .eq(AiChatMessage::getSessionId, sessionId));
        sessionMapper.deleteById(sessionId);
    }

    @Override
    public String getContextPrompt(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) return "用户信息不存在";

        // 只取近期有库存的课程，避免 prompt 过长消耗过多 token
        List<GymCourse> courses = courseMapper.selectList(new LambdaQueryWrapper<GymCourse>()
                .ge(GymCourse::getStartTime, LocalDateTime.now())
                .gt(GymCourse::getStock, 0)
                .orderByAsc(GymCourse::getStartTime)
                .last("LIMIT 5"));

        List<BookingVO> recentBookings = bookingMapper.selectMyBookings(userId);

        StringBuilder sb = new StringBuilder();
        sb.append("用户名：").append(user.getUsername()).append("\n");
        sb.append("账户余额：").append(user.getBalance()).append("元\n");

        String vipDesc;
        if (user.getVipType() != null && user.getVipType() == 1) vipDesc = "月卡VIP（享9折优惠）";
        else if (user.getVipType() != null && user.getVipType() == 2) vipDesc = "年卡VIP（享8折优惠）";
        else vipDesc = "普通会员（无折扣）";
        sb.append("会员状态：").append(vipDesc).append("\n");

        sb.append("\n近期可预约课程（按时间排序）：\n");
        if (CollUtil.isEmpty(courses)) {
            sb.append("暂无近期课程\n");
        } else {
            for (GymCourse c : courses) {
                sb.append(String.format("- 【%s】教练:%s，价格:%.0f元，剩余名额:%d，时间:%s\n",
                        c.getName(), c.getCoach(), c.getPrice(), c.getStock(),
                        c.getStartTime().format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))));
                if (StrUtil.isNotBlank(c.getContent())) {
                    sb.append("  简介：").append(c.getContent()).append("\n");
                }
            }
        }

        sb.append("\n用户最近预约记录（最近2条）：\n");
        if (CollUtil.isEmpty(recentBookings)) {
            sb.append("暂无预约记录\n");
        } else {
            recentBookings.stream().limit(2).forEach(b -> sb.append(String.format("- %s（%s）\n",
                    StrUtil.nullToEmpty(b.getCourseName()),
                    b.getStartTime() != null
                            ? b.getStartTime().format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))
                            : "时间未知")));
        }

        return sb.toString();
    }

    // ======================== 私有工具方法 ========================

    private AiChatSession getOrCreateSession(Long userId, Long sessionId, String message) {
        if (sessionId == null) {
            // sessionId 为 null 表示新建会话，取消息前 20 字作为会话标题
            AiChatSession session = new AiChatSession();
            session.setUserId(userId);
            session.setTitle(message.length() > 20 ? message.substring(0, 20) : message);
            session.setCreateTime(LocalDateTime.now());
            session.setUpdateTime(LocalDateTime.now());
            sessionMapper.insert(session);
            return session;
        }
        AiChatSession session = sessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "会话不存在");
        }
        return session;
    }

    private void saveAssistantMessage(Long sessionId, String content) {
        AiChatMessage msg = new AiChatMessage();
        msg.setSessionId(sessionId);
        msg.setRole("assistant");
        msg.setContent(content);
        msg.setCreateTime(LocalDateTime.now());
        messageMapper.insert(msg);

        AiChatSession session = sessionMapper.selectById(sessionId);
        if (session != null) {
            session.setUpdateTime(LocalDateTime.now());
            sessionMapper.updateById(session);
        }
    }

    private String buildRequestBody(Long sessionId, String context, boolean stream) {
        // 用 replace 而非 String.format，避免 context 含 % 字符时触发 FormatException
        String systemPrompt = SYSTEM_PROMPT_TEMPLATE.replace("%s", context);

        JSONArray messages = new JSONArray();
        messages.put(new JSONObject().set("role", "system").set("content", systemPrompt));

        // 倒序取最近 HISTORY_LIMIT 条后翻转，保证发给 DeepSeek 的消息仍是时间正序
        List<AiChatMessage> history = messageMapper.selectList(
                new LambdaQueryWrapper<AiChatMessage>()
                        .eq(AiChatMessage::getSessionId, sessionId)
                        .orderByDesc(AiChatMessage::getCreateTime)
                        .last("LIMIT " + HISTORY_LIMIT));
        Collections.reverse(history);

        for (AiChatMessage h : history) {
            messages.put(new JSONObject().set("role", h.getRole()).set("content", h.getContent()));
        }

        return new JSONObject()
                .set("model", deepSeekConfig.getModel())
                .set("messages", messages)
                .set("stream", stream)
                .set("temperature", deepSeekConfig.getTemperature())
                .toString();
    }

    private String callDeepSeekSync(Long sessionId, String context) throws Exception {
        String url = deepSeekConfig.getBaseUrl() + "/chat/completions";
        String requestBody = buildRequestBody(sessionId, context, false);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .header("Authorization", "Bearer " + deepSeekConfig.getApiKey())
                .header("Content-Type", "application/json")
                .timeout(Duration.ofMillis(deepSeekConfig.getTimeoutMs()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("DeepSeek HTTP " + response.statusCode() + ": " + response.body());
        }

        JSONObject json = JSONUtil.parseObj(response.body());
        JSONArray choices = json.getJSONArray("choices");
        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("DeepSeek 响应无 choices");
        }
        String content = choices.getJSONObject(0).getJSONObject("message").getStr("content");
        if (StrUtil.isBlank(content)) {
            throw new RuntimeException("DeepSeek 响应 content 为空");
        }
        return content;
    }

    /**
     * 调用 DeepSeek 流式接口（stream=true），逐行解析 SSE 格式的响应。
     * 每行格式为 "data: {...json...}"，取 choices[0].delta.content 回调给调用方。
     */
    private void callDeepSeekStream(Long sessionId, String context, ChunkConsumer consumer) throws Exception {
        String url = deepSeekConfig.getBaseUrl() + "/chat/completions";
        String requestBody = buildRequestBody(sessionId, context, true);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .header("Authorization", "Bearer " + deepSeekConfig.getApiKey())
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(60))
                .build();

        HttpResponse<Stream<String>> response = client.send(request, HttpResponse.BodyHandlers.ofLines());
        if (response.statusCode() != 200) {
            throw new RuntimeException("DeepSeek HTTP " + response.statusCode());
        }

        response.body().forEach(line -> {
            if (!line.startsWith("data: ")) return;
            String data = line.substring(6).trim();
            if (StrUtil.isBlank(data) || "[DONE]".equals(data)) return;
            try {
                JSONObject json = JSONUtil.parseObj(data);
                JSONArray choices = json.getJSONArray("choices");
                if (choices == null || choices.isEmpty()) return;
                JSONObject delta = choices.getJSONObject(0).getJSONObject("delta");
                if (delta == null) return;
                String content = delta.getStr("content");
                if (StrUtil.isNotEmpty(content)) {
                    consumer.accept(content);
                }
            } catch (ClientDisconnectedException e) {
                throw e;
            } catch (Exception e) {
                log.debug("解析流式响应行异常（已跳过）: {}", e.getMessage());
            }
        });
    }

    @FunctionalInterface
    private interface ChunkConsumer {
        void accept(String chunk);
    }

    private static class ClientDisconnectedException extends RuntimeException {
        ClientDisconnectedException() {
            super("client disconnected", null, true, false);
        }
    }
}
