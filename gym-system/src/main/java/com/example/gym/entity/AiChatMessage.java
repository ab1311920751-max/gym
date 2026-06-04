package com.example.gym.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * AI 聊天消息实体，对应数据库表 ai_chat_message。
 * 记录每次对话中用户和 AI 的消息内容，按时间正序排列。
 * 每次请求时携带最近 20 条（HISTORY_LIMIT）历史消息作为上下文发送给 DeepSeek。
 */
@Data
@TableName("ai_chat_message")
public class AiChatMessage {
    /** 主键，自增 */
    @TableId(type = IdType.AUTO)
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
