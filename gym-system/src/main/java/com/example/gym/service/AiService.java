package com.example.gym.service;

import com.example.gym.vo.AiChatMessageVO;
import com.example.gym.vo.AiChatSessionVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * AI 客服业务接口，提供同步和流式两种对话模式，以及会话管理功能。
 * 底层调用 DeepSeek API，需在 application.yml 中配置 deepseek.enabled=true 才生效。
 */
public interface AiService {

    /**
     * 同步对话，等待 DeepSeek 返回完整回复后一次性响应。
     * sessionId 为 null 时自动创建新会话，会话标题取消息前 20 字。
     *
     * @return 包含 AI 回复内容及 sessionId 的 VO
     */
    AiChatMessageVO chat(Long userId, Long sessionId, String message);

    /**
     * 流式对话，通过 SSE（Server-Sent Events）逐块推送 DeepSeek 的回复。
     * 首帧发送 sessionId（新建会话时前端需要），末帧发送 [DONE] 通知前端结束。
     * 客户端断连时保存已推送的部分内容，避免记录丢失。
     */
    void chatStream(Long userId, Long sessionId, String message, SseEmitter emitter);

    /** 查询用户的全部会话列表，按最后更新时间倒序，每条带最后一条消息预览 */
    List<AiChatSessionVO> getSessions(Long userId);

    /** 查询指定会话的全部消息记录，按时间正序，校验会话归属防止越权 */
    List<AiChatMessageVO> getMessages(Long userId, Long sessionId);

    /** 删除会话及其下所有消息，校验归属后在同一事务内级联删除 */
    void deleteSession(Long userId, Long sessionId);

    /**
     * 构建当前用户的上下文 Prompt，内容包括：余额、VIP 状态、
     * 近期可预约课程（最多 5 条）、最近 2 条预约记录。
     * 注入到 System Prompt 中，让 AI 能根据个人信息给出个性化推荐。
     */
    String getContextPrompt(Long userId);
}
