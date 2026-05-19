package com.example.gym.dto;

import lombok.Data;

@Data
public class AiChatReq {
    private Long sessionId;
    private String message;
}