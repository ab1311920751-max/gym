package com.example.gym.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.gym.entity.GymCourse;
import com.example.gym.mapper.CourseMapper;
import com.example.gym.service.CourseService;
import org.springframework.stereotype.Service;

@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, GymCourse> implements CourseService {
}