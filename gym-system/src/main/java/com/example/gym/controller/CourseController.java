package com.example.gym.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.gym.common.Result;
import com.example.gym.entity.GymCourse;
import com.example.gym.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    // --- 🔥 核心修复：必须显式定义 /list 接口 ---
    // 否则前端查所有课程时，会报错 "Failed to convert value of type String to Long"
    @GetMapping("/list")
    public Result list() {
        return Result.success(courseService.list());
    }

    // 新增
    @PostMapping
    public Result save(@RequestBody GymCourse course) {
        courseService.save(course);
        return Result.success();
    }

    // 修改
    @PutMapping
    public Result update(@RequestBody GymCourse course) {
        courseService.updateById(course);
        return Result.success();
    }

    // 删除
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        courseService.removeById(id);
        return Result.success();
    }

    // 根据ID查单个
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        return Result.success(courseService.getById(id));
    }

    // 分页查询
    @GetMapping("/page")
    public Result findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                           @RequestParam(defaultValue = "10") Integer pageSize,
                           @RequestParam(required = false) String name) {
        LambdaQueryWrapper<GymCourse> query = new LambdaQueryWrapper<>();
        query.like(StrUtil.isNotBlank(name), GymCourse::getName, name);
        query.orderByDesc(GymCourse::getId);
        return Result.success(courseService.page(new Page<>(pageNum, pageSize), query));
    }
}