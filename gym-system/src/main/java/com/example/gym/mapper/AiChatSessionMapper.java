package com.example.gym.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.gym.entity.AiChatSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 会话 Mapper，继承 MyBatis-Plus BaseMapper&lt;AiChatSession&gt;，自动拥有通用 CRUD 能力。
 * 暂无自定义 SQL，会话查询通过 LambdaQueryWrapper 在 Service 层构建。
 */
@Mapper
public interface AiChatSessionMapper extends BaseMapper<AiChatSession> {
}
