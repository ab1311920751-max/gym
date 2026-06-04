package com.example.gym.vo;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * AI 会话列表视图对象，用于前端展示会话列表。
 * lastMessage 取最后一条消息的前 30 字作为预览，updateTime 按最后消息时间排序。
 */
@Data
@Builder
public class AiChatSessionVO {
    /** 会话 ID */
    private Long id;

    /** 会话标题 */
    private String title;

    /** 最后一条消息的前 30 字预览，用于列表展示 */
    private String lastMessage;

    /** 会话最后更新时间 */
    private LocalDateTime updateTime;
}
