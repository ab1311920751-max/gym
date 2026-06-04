package com.example.gym.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.gym.entity.GymCourse;

/**
 * 课程 Mapper，继承 MyBatis-Plus BaseMapper&lt;GymCourse&gt;，自动拥有通用 CRUD 能力。
 * 暂无自定义 SQL，课程查询通过 LambdaQueryWrapper 在 Service 层构建。
 */
public interface CourseMapper extends BaseMapper<GymCourse> {
}