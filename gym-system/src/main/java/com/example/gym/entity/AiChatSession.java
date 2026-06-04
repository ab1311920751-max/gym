package com.example.gym.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * AI 聊天会话实体，对应数据库表 ai_chat_session。
 * 每个用户可以创建多个会话，每个会话包含多条消息（ai_chat_message）。
 * 会话标题在新建时取用户第一条消息的前 20 个字符。
 */
@Data
@TableName("ai_chat_session")
public class AiChatSession {
    /** 主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户 ID */
    private Long userId;

    /** 会话标题，新建会话时取用户第一条消息的前 20 个字符 */
    private String title;

    /** 会话创建时间 */
    private LocalDateTime createTime;

    /** 会话最后更新时间（最近一条消息的时间） */
    private LocalDateTime updateTime;
}
