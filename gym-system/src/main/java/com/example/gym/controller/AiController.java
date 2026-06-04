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

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/chat")
    public Result<AiChatMessageVO> chat(@CurrentUserId Long uid, @RequestBody AiChatReq req) {
        AiChatMessageVO reply = aiService.chat(uid, req.getSessionId(), req.getMessage());
        return Result.success(reply);
    }

    @PostMapping("/chat/stream")
    public SseEmitter chatStream(@CurrentUserId Long uid, @RequestBody AiChatReq req) {
        SseEmitter emitter = new SseEmitter(60_000L);
        aiService.chatStream(uid, req.getSessionId(), req.getMessage(), emitter);
        return emitter;
    }

    @GetMapping("/sessions")
    public Result<List<AiChatSessionVO>> getSessions(@CurrentUserId Long uid) {
        return Result.success(aiService.getSessions(uid));
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public Result<List<AiChatMessageVO>> getMessages(@CurrentUserId Long uid, @PathVariable Long sessionId) {
        return Result.success(aiService.getMessages(uid, sessionId));
    }

    @DeleteMapping("/sessions/{sessionId}")
    public Result<Void> deleteSession(@CurrentUserId Long uid, @PathVariable Long sessionId) {
        aiService.deleteSession(uid, sessionId);
        return Result.success();
    }
}
