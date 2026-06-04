package com.example.gym.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.gym.entity.GymCourse;

/**
 * 课程业务接口。
 * 目前所有 CRUD 操作均由 MyBatis-Plus IService 提供，Controller 直接调用，
 * 后续如需添加课程上下架、库存预警等业务逻辑，在此扩展方法。
 */
public interface CourseService extends IService<GymCourse> {
}
