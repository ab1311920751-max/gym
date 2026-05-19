package com.example.gym.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.gym.config.DeepSeekConfig;
import com.example.gym.entity.GymCourse;
import com.example.gym.entity.SysUser;
import com.example.gym.mapper.CourseMapper;
import com.example.gym.mapper.UserMapper;
import com.example.gym.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final UserMapper userMapper;
    private final CourseMapper courseMapper;
    private final DeepSeekConfig deepSeekConfig;

    /**
     * 核心方法：AI 对话
     * 优先调用 DeepSeek（带业务上下文的 RAG），失败时降级为本地规则 AI 保证演示稳定。
     */
    @Override
    public String chat(Long userId, String message) {
        String context = getContextPrompt(userId);

        if (deepSeekConfig.isEnabled()) {
            try {
                return callDeepSeekApi(context, message);
            } catch (Exception e) {
                log.warn("DeepSeek API 调用失败，降级到本地规则 AI: {}", e.getMessage());
            }
        }
        return localRuleBasedAi(userId, message, context);
    }

    /**
     * 调用 DeepSeek Chat Completions（OpenAI 兼容协议）。
     * 业务上下文作为 system prompt 注入，用户输入作为 user message。
     */
    private String callDeepSeekApi(String context, String userMessage) {
        String url = deepSeekConfig.getBaseUrl() + "/chat/completions";

        JSONObject systemMsg = new JSONObject()
                .set("role", "system")
                .set("content", "你是健身房预约系统的 AI 私教助手，回答要简洁、口语化、用中文，"
                        + "并优先结合下方业务上下文给出基于用户实际数据的建议。\n\n" + context);
        JSONObject userMsg = new JSONObject()
                .set("role", "user")
                .set("content", userMessage);

        JSONObject body = new JSONObject()
                .set("model", deepSeekConfig.getModel())
                .set("messages", new JSONArray().put(systemMsg).put(userMsg))
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

    /**
     * 构建 Prompt 上下文
     * 让 AI 知道你是谁，你有多少钱，现在有哪些课
     */
    @Override
    public String getContextPrompt(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) return "用户不存在";

        // 查询未来可预约的课程
        List<GymCourse> courses = courseMapper.selectList(new LambdaQueryWrapper<GymCourse>()
                .ge(GymCourse::getStartTime, LocalDateTime.now())
                .gt(GymCourse::getStock, 0) // 只看有库存的
                .orderByAsc(GymCourse::getStartTime)
                .last("LIMIT 5")); // 避免 Prompt 太长，只取最近5节

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
                sb.append(String.format("- %s | 教练:%s | 价格:%.0f元 | 剩余:%d | 时间:%s\n",
                        c.getName(), c.getCoach(), c.getPrice(), c.getStock(), c.getStartTime()));
            }
        }

        return sb.toString();
    }

    private String localRuleBasedAi(Long userId, String message, String context) {
        SysUser user = userMapper.selectById(userId);

        // 关键词识别
        if (message.contains("推荐") || message.contains("什么课")) {
            return "根据您的余额 (" + user.getBalance() + "元) 和 VIP 权益，我为您分析了最近的课程数据：\n\n"
                    + extractCourseRecommendation(context, user.getBalance());
        }

        if (message.contains("余额") || message.contains("多少钱")) {
            return "您当前的账户余额为 " + user.getBalance() + " 元。"
                    + (user.getBalance().compareTo(new BigDecimal("100")) < 0 ? " 余额稍显不足，建议充值以防抢课失败。" : " 资金充足，快去抢课吧！");
        }

        if (message.contains("VIP") || message.contains("会员")) {
            if (user.getVipType() == 0) return "您目前是普通会员。开通月卡可享9折，年卡8折！";
            return "尊贵的 " + getVipName(user.getVipType()) + "，您的权益正在生效中。需要续费吗？";
        }

        return "我是您的专属 AI 教练助手。我可以为您推荐课程、查询余额或解答会员权益问题。\n(尝试问我：\"给我推荐几节课\")";
    }

    private String extractCourseRecommendation(String context, BigDecimal balance) {
        // 简单的解析逻辑，实际项目中这里会发给 LLM
        if (context.contains("暂无近期课程")) return "最近好像没有排课，请联系管理员。";

        // 截取课程部分
        String coursePart = StrUtil.subAfter(context, "【推荐课程列表】\n", false);
        return coursePart + "\n(Tips: 结合您的余额，建议优先选择性价比高的课程)";
    }

    private String getVipName(Integer type) {
        if (type == 1) return "月卡VIP";
        if (type == 2) return "年卡VIP";
        return "普通会员";
    }
}