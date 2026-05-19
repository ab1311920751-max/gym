-- AI 智能客服功能 - 数据库迁移脚本
-- 在 gym_db 数据库上执行

CREATE TABLE IF NOT EXISTS `ai_chat_session` (
    `id`          BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT(20)   NOT NULL COMMENT '用户ID',
    `title`       VARCHAR(100) DEFAULT '新对话' COMMENT '会话标题',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后活跃时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI客服会话表';

CREATE TABLE IF NOT EXISTS `ai_chat_message` (
    `id`          BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `session_id`  BIGINT(20)   NOT NULL COMMENT '会话ID',
    `role`        VARCHAR(20)  NOT NULL COMMENT '角色：user / assistant',
    `content`     TEXT         NOT NULL COMMENT '消息内容',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    PRIMARY KEY (`id`),
    INDEX `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI客服消息表';
