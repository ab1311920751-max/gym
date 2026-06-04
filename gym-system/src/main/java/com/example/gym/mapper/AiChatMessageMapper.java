package com.example.gym.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.gym.entity.AiChatMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 消息 Mapper，继承 MyBatis-Plus BaseMapper&lt;AiChatMessage&gt;，自动拥有通用 CRUD 能力。
 * 暂无自定义 SQL，消息查询通过 LambdaQueryWrapper 在 Service 层构建。
 */
@Mapper
public interface AiChatMessageMapper extends BaseMapper<AiChatMessage> {
}
