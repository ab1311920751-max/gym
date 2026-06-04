package com.example.gym.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.gym.entity.GymCourse;
import com.example.gym.mapper.CourseMapper;
import com.example.gym.service.CourseService;
import org.springframework.stereotype.Service;

/**
 * 课程业务实现，继承 MyBatis-Plus ServiceImpl，自动获得 list()、getById()、save()、
 * updateById()、removeById() 等通用 CRUD 方法。
 * 当前所有课程操作均通过 Controller 直接调用父类方法，无需自定义业务逻辑。
 */
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, GymCourse> implements CourseService {
}