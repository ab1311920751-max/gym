package com.example.gym.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
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
import com.example.gym.mapper.CourseMapper;
import com.example.gym.mapper.UserMapper;
import com.example.gym.service.AiService;
import com.example.gym.vo.AiChatMessageVO;
import com.example.gym.vo.AiChatSessionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private static final int HISTORY_LIMIT = 10;

    private final UserMapper userMapper;
    private final CourseMapper courseMapper;
    private final AiChatSessionMapper sessionMapper;
    private final AiChatMessageMapper messageMapper;
    private final DeepSeekConfig deepSeekConfig;

    @Override
    @Transactional
    public AiChatMessageVO chat(Long userId, Long sessionId, String message) {
        if (StrUtil.isBlank(message)) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "消息不能为空");
        }

        AiChatSession session;
        if (sessionId == null) {
            session = new AiChatSession();
            session.setUserId(userId);
            session.setTitle(message.length() > 20 ? message.substring(0, 20) : message);
            session.setCreateTime(LocalDateTime.now());
            session.setUpdateTime(LocalDateTime.now());
            sessionMapper.insert(session);
        } else {
            session = sessionMapper.selectById(sessionId);
            if (session == null || !session.getUserId().equals(userId)) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "会话不存在");
            }
        }

        AiChatMessage userMsg = new AiChatMessage();
        userMsg.setSessionId(session.getId());
        userMsg.setRole("user");
        userMsg.setContent(message);
        userMsg.setCreateTime(LocalDateTime.now());
        messageMapper.insert(userMsg);

        String context = getContextPrompt(userId);
        String reply;
        if (deepSeekConfig.isEnabled()) {
            try {
                reply = callDeepSeekApi(session.getId(), context, message);
            } catch (Exception e) {
                log.warn("DeepSeek API 调用失败，降级到本地规则 AI: {}", e.getMessage());
                reply = localRuleBasedAi(userId, message, context);
            }
        } else {
            reply = localRuleBasedAi(userId, message, context);
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
                    ? (lastMsg.getContent().length() > 30 ? lastMsg.getContent().substring(0, 30) : lastMsg.getContent())
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

    // ======================== AI 调用 ========================

    private String callDeepSeekApi(Long sessionId, String context, String userMessage) {
        String url = deepSeekConfig.getBaseUrl() + "/chat/completions";

        JSONArray messages = new JSONArray();
        messages.put(new JSONObject()
                .set("role", "system")
                .set("content", "你是健身房预约系统的 AI 智能客服，回答要简洁、口语化、用中文，"
                        + "优先结合下方业务上下文给出建议。你只能提供课程信息查询和推荐，"
                        + "不能代为预约、下单或支付。如果用户要求预约，引导他们去「预约课程」页面自行操作。\n\n" + context));

        List<AiChatMessage> history = messageMapper.selectList(
                new LambdaQueryWrapper<AiChatMessage>()
                        .eq(AiChatMessage::getSessionId, sessionId)
                        .eq(AiChatMessage::getRole, "user")
                        .orderByDesc(AiChatMessage::getCreateTime)
                        .last("LIMIT " + HISTORY_LIMIT));
        List<AiChatMessage> assistantHistory = messageMapper.selectList(
                new LambdaQueryWrapper<AiChatMessage>()
                        .eq(AiChatMessage::getSessionId, sessionId)
                        .eq(AiChatMessage::getRole, "assistant")
                        .orderByDesc(AiChatMessage::getCreateTime)
                        .last("LIMIT " + HISTORY_LIMIT));

        List<AiChatMessage> combined = new ArrayList<>();
        int ui = history.size() - 1;
        int ai = assistantHistory.size() - 1;
        while (ui >= 0 && ai >= 0) {
            combined.add(0, assistantHistory.get(ai));
            combined.add(0, history.get(ui));
            ui--;
            ai--;
        }
        while (ui >= 0) {
            combined.add(0, history.get(ui));
            ui--;
        }

        for (AiChatMessage h : combined) {
            messages.put(new JSONObject()
                    .set("role", h.getRole())
                    .set("content", h.getContent()));
        }

        JSONObject body = new JSONObject()
                .set("model", deepSeekConfig.getModel())
                .set("messages", messages)
                .set("stream", false)
                .set("temperature", deepSeekConfig.getTemperature());

        try (HttpResponse response = HttpRequest.post(url)
                .header("Authorization", "Bearer " + deepSeekConfig.getApiKey())
                .header("Content-Type", "application/json")
                .timeout(deepSeekConfig.getTimeoutMs())
                .body(body.toString())
                .execute()) {

            String respBody = response.body();
            if (response.getStatus() != 200) {
                throw new RuntimeException("DeepSeek HTTP " + response.getStatus() + ": " + respBody);
            }

            JSONObject json = JSONUtil.parseObj(respBody);
            JSONArray choices = json.getJSONArray("choices");
            if (choices == null || choices.isEmpty()) {
                throw new RuntimeException("DeepSeek 响应无 choices: " + respBody);
            }
            String content = choices.getJSONObject(0).getJSONObject("message").getStr("content");
            if (StrUtil.isBlank(content)) {
                throw new RuntimeException("DeepSeek 响应 content 为空");
            }
            return content;
        }
    }

    // ======================== 上下文构建 ========================

    @Override
    public String getContextPrompt(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) return "用户不存在";

        List<GymCourse> courses = courseMapper.selectList(new LambdaQueryWrapper<GymCourse>()
                .ge(GymCourse::getStartTime, LocalDateTime.now())
                .gt(GymCourse::getStock, 0)
                .orderByAsc(GymCourse::getStartTime)
                .last("LIMIT 5"));

        StringBuilder sb = new StringBuilder();
        sb.append("【用户画像】\n");
        sb.append("姓名：").append(user.getUsername()).append("\n");
        sb.append("余额：").append(user.getBalance()).append("元\n");
        sb.append("VIP等级：").append(getVipName(user.getVipType())).append("\n");

        sb.append("\n【推荐课程列表】\n");
        if (CollUtil.isEmpty(courses)) {
            sb.append("暂无近期课程\n");
        } else {
            for (GymCourse c : courses) {
                sb.append(String.format("- %s | 教练:%s | 价格:%.0f元 | 剩余名额:%d | 时间:%s | 简介:%s\n",
                        c.getName(), c.getCoach(), c.getPrice(), c.getStock(),
                        c.getStartTime().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")),
                        StrUtil.nullToEmpty(c.getContent())));
            }
        }

        return sb.toString();
    }

    // ======================== 本地规则兜底 ========================

    private String localRuleBasedAi(Long userId, String message, String context) {
        SysUser user = userMapper.selectById(userId);

        List<GymCourse> allCourses = courseMapper.selectList(new LambdaQueryWrapper<GymCourse>()
                .ge(GymCourse::getStartTime, LocalDateTime.now())
                .gt(GymCourse::getStock, 0)
                .orderByAsc(GymCourse::getStartTime));

        // 课程名称匹配
        for (GymCourse c : allCourses) {
            if (message.contains(c.getName())) {
                return String.format("【%s】\n教练：%s\n价格：%.0f元\n剩余名额：%d\n时间：%s\n简介：%s\n\n%s",
                        c.getName(), c.getCoach(), c.getPrice(), c.getStock(),
                        c.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                        StrUtil.nullToEmpty(c.getContent()),
                        c.getPrice().compareTo(BigDecimal.ZERO) == 0 ? "该课程免费，快去「预约课程」页面报名吧！" : "感兴趣的话，去「预约课程」页面下单吧！");
            }
        }

        // 教练名称匹配
        for (GymCourse c : allCourses) {
            if (StrUtil.isNotBlank(c.getCoach()) && message.contains(c.getCoach())) {
                List<GymCourse> coachCourses = allCourses.stream()
                        .filter(x -> c.getCoach().equals(x.getCoach()))
                        .collect(Collectors.toList());
                StringBuilder sb = new StringBuilder("教练【" + c.getCoach() + "】的课程：\n");
                for (GymCourse cc : coachCourses) {
                    sb.append(String.format("- %s | %.0f元 | %s | 剩余%d\n",
                            cc.getName(), cc.getPrice(),
                            cc.getStartTime().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")),
                            cc.getStock()));
                }
                return sb.toString();
            }
        }

        // 时间相关
        if (message.contains("明天") || message.contains("明天有")) {
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            List<GymCourse> dayCourses = allCourses.stream()
                    .filter(c -> c.getStartTime().toLocalDate().equals(tomorrow))
                    .collect(Collectors.toList());
            if (dayCourses.isEmpty()) {
                return "明天暂时没有课程安排，您可以看看其他时间的课程。";
            }
            StringBuilder sb = new StringBuilder("明天（" + tomorrow + "）的课程：\n");
            for (GymCourse c : dayCourses) {
                sb.append(String.format("- %s | %s | %.0f元 | 剩余%d\n",
                        c.getName(), c.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                        c.getPrice(), c.getStock()));
            }
            return sb.toString();
        }

        if (message.contains("今天") || message.contains("今天有")) {
            LocalDate today = LocalDate.now();
            List<GymCourse> dayCourses = allCourses.stream()
                    .filter(c -> c.getStartTime().toLocalDate().equals(today))
                    .collect(Collectors.toList());
            if (dayCourses.isEmpty()) {
                return "今天暂时没有课程安排了，看看明天的课吧。";
            }
            StringBuilder sb = new StringBuilder("今天（" + today + "）的课程：\n");
            for (GymCourse c : dayCourses) {
                sb.append(String.format("- %s | %s | %.0f元 | 剩余%d\n",
                        c.getName(), c.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                        c.getPrice(), c.getStock()));
            }
            return sb.toString();
        }

        // 库存/名额相关
        if (message.contains("名额") || message.contains("库存") || message.contains("还有")) {
            if (allCourses.isEmpty()) {
                return "目前没有可预约的课程。";
            }
            StringBuilder sb = new StringBuilder("当前可预约课程名额：\n");
            for (GymCourse c : allCourses) {
                String warn = c.getStock() <= 3 ? " ⚠️即将售罄" : "";
                sb.append(String.format("- %s：剩余%d/%d%s\n", c.getName(), c.getStock(), c.getCapacity(), warn));
            }
            return sb.toString();
        }

        // 推荐
        if (message.contains("推荐") || message.contains("什么课")) {
            return "根据您的余额 (" + user.getBalance() + "元) 和 VIP 权益，我为您分析了最近的课程：\n\n"
                    + extractCourseRecommendation(context, user.getBalance());
        }

        // 余额
        if (message.contains("余额") || message.contains("多少钱")) {
            return "您当前的账户余额为 " + user.getBalance() + " 元。"
                    + (user.getBalance().compareTo(new BigDecimal("100")) < 0 ? " 余额稍显不足，建议充值以防抢课失败。" : " 资金充足，快去抢课吧！");
        }

        // VIP
        if (message.contains("VIP") || message.contains("会员")) {
            if (user.getVipType() == 0) return "您目前是普通会员。开通月卡（30元/月）可享9折，年卡（300元/年）可享8折！需要办理吗？";
            return "尊贵的 " + getVipName(user.getVipType()) + "，您的权益正在生效中。需要续费或了解权益详情吗？";
        }

        return "我是您的专属 AI 健身客服。您可以问我：\n"
                + "• 课程推荐（说「推荐课程」）\n"
                + "• 特定课程详情（输入课程名称）\n"
                + "• 教练课程（说「XX教练有什么课」）\n"
                + "• 时间查询（说「明天有什么课」）\n"
                + "• 名额查询（说「还有名额吗」）\n"
                + "• 余额/VIP 查询";
    }

    private String extractCourseRecommendation(String context, BigDecimal balance) {
        if (context.contains("暂无近期课程")) return "最近好像没有排课，请联系管理员。";
        String coursePart = StrUtil.subAfter(context, "【推荐课程列表】\n", false);
        return coursePart + "\n(Tips: 结合您的余额，建议优先选择性价比高的课程)";
    }

    private String getVipName(Integer type) {
        if (type == 1) return "月卡VIP";
        if (type == 2) return "年卡VIP";
        return "普通会员";
    }
}
