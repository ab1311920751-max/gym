package com.example.gym.dto;

import lombok.Data;

/**
 * AI 聊天请求 DTO，前端发送聊天消息时使用。
 * sessionId 为 null 时表示新建会话，服务端自动创建并返回新会话 ID。
 */
@Data
public class AiChatReq {
    /** 会话 ID，为 null 时表示新建会话 */
    private Long sessionId;

    /** 用户发送的消息内容，不能为空 */
    private String message;
}