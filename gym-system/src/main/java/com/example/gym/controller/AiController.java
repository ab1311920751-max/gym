package com.example.gym.controller;

import com.example.gym.common.Result;
import com.example.gym.common.auth.CurrentUserId;
import com.example.gym.dto.AiChatReq;
import com.example.gym.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/chat")
    public Result chat(@CurrentUserId Long uid, @RequestBody AiChatReq req) {
        try { Thread.sleep(800); } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        String response = aiService.chat(uid, req.getMessage());
        return Result.success(response);
    }
}