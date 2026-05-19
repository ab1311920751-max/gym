package com.example.gym.dto;

import lombok.Data;

@Data
public class AiChatReq {
    private Long userId;
    private String message;
}