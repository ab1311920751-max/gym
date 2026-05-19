package com.example.gym.service;

import com.example.gym.vo.AiChatMessageVO;
import com.example.gym.vo.AiChatSessionVO;

import java.util.List;

public interface AiService {
    AiChatMessageVO chat(Long userId, Long sessionId, String message);

    List<AiChatSessionVO> getSessions(Long userId);

    List<AiChatMessageVO> getMessages(Long userId, Long sessionId);

    void deleteSession(Long userId, Long sessionId);

    String getContextPrompt(Long userId);
}
