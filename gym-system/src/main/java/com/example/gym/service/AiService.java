package com.example.gym.service;

import java.util.Map;

public interface AiService {
    /**
     * 获取 AI 对话响应
     * @param userId 用户ID
     * @param message 用户发来的消息
     * @return AI 的回复内容
     */
    String chat(Long userId, String message);

    /**
     * (Debug用) 获取组装好的上下文提示词
     */
    String getContextPrompt(Long userId);
}