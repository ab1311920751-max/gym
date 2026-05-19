package com.example.gym.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.gym.entity.SysUser;

// 继承 BaseMapper 后，自动拥有 CRUD 能力
public interface UserMapper extends BaseMapper<SysUser> {
}