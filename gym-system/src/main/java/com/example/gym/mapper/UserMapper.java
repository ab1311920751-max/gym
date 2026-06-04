package com.example.gym.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.gym.entity.SysUser;

/**
 * 系统用户 Mapper，继承 MyBatis-Plus BaseMapper&lt;SysUser&gt;，自动拥有通用 CRUD 能力。
 * 暂无自定义 SQL，用户查询通过 LambdaQueryWrapper 在 Service 层构建。
 */
public interface UserMapper extends BaseMapper<SysUser> {
}