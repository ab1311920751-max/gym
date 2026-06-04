package com.example.gym.vo;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * AI 聊天消息视图对象，用于前端展示单条消息。
 * role 区分用户消息（"user"）和 AI 回复（"assistant"），消息按 createTime 正序排列。
 */
@Data
@Builder
public class AiChatMessageVO {
    /** 消息 ID */
    private Long id;

    /** 所属会话 ID */
    private Long sessionId;

    /** 消息角色："user" 用户消息，"assistant" AI 回复 */
    private String role;

    /** 消息文本内容 */
    private String content;

    /** 消息创建时间 */
    private LocalDateTime createTime;
}
