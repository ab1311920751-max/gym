package com.example.gym.controller;

import com.example.gym.common.Result;
import com.example.gym.common.auth.CurrentUserId;
import com.example.gym.dto.AiChatReq;
import com.example.gym.service.AiService;
import com.example.gym.vo.AiChatMessageVO;
import com.example.gym.vo.AiChatSessionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * AI 聊天控制器，提供 DeepSeek AI 客服的 HTTP 接口。
 * <p>
 * 包含五个接口：
 * <ul>
 *   <li>POST /ai/chat — 同步聊天，等 DeepSeek 返回完整回复后一次性响应</li>
 *   <li>POST /ai/chat/stream — SSE 流式聊天（打字机效果），返回 SseEmitter</li>
 *   <li>GET /ai/sessions — 当前用户的会话列表</li>
 *   <li>GET /ai/sessions/{id}/messages — 指定会话的消息历史</li>
 *   <li>DELETE /ai/sessions/{id} — 删除会话及其所有消息</li>
 * </ul>
 * 所有接口均通过 @CurrentUserId 从 JWT 获取当前用户，确保只能访问自己的会话和消息。
 */
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    /** 普通同步接口，等 DeepSeek 返回完整回复后一次性响应 */
    @PostMapping("/chat")
    public Result<AiChatMessageVO> chat(@CurrentUserId Long uid, @RequestBody AiChatReq req) {
        AiChatMessageVO reply = aiService.chat(uid, req.getSessionId(), req.getMessage());
        return Result.success(reply);
    }

    /**
     * SSE 流式接口，返回 SseEmitter 而非 Result。
     * 前端通过 EventSource 或 fetch+ReadableStream 消费，每个 chunk 追加到消息气泡末尾，
     * 实现打字机效果。超时时间设 60s，覆盖 DeepSeek 最慢响应场景。
     */
    @PostMapping("/chat/stream")
    public SseEmitter chatStream(@CurrentUserId Long uid, @RequestBody AiChatReq req) {
        SseEmitter emitter = new SseEmitter(60_000L);
        // 在新线程中推送，避免阻塞 Tomcat 线程
        aiService.chatStream(uid, req.getSessionId(), req.getMessage(), emitter);
        return emitter;
    }

    /** 查询当前用户的所有 AI 会话列表，按更新时间倒序，每个会话附带最后一条消息的预览 */
    @GetMapping("/sessions")
    public Result<List<AiChatSessionVO>> getSessions(@CurrentUserId Long uid) {
        return Result.success(aiService.getSessions(uid));
    }

    /** 查询指定会话的所有消息，按时间正序排列，用于前端恢复对话记录 */
    @GetMapping("/sessions/{sessionId}/messages")
    public Result<List<AiChatMessageVO>> getMessages(@CurrentUserId Long uid, @PathVariable Long sessionId) {
        return Result.success(aiService.getMessages(uid, sessionId));
    }

    /** 删除指定会话及其所有关联消息，仅会话所有者可操作 */
    @DeleteMapping("/sessions/{sessionId}")
    public Result<Void> deleteSession(@CurrentUserId Long uid, @PathVariable Long sessionId) {
        aiService.deleteSession(uid, sessionId);
        return Result.success();
    }
}
