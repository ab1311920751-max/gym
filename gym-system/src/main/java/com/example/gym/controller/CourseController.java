package com.example.gym.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.gym.common.Result;
import com.example.gym.entity.GymCourse;
import com.example.gym.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程控制器，提供课程的查询与管理能力。
 * 列表接口（/list、/page）均支持按分类筛选，category 的合法值由前端
 * src/constants/course.js 中 COURSE_CATEGORIES 定义，前后端须保持一致。
 */
@RestController
@RequestMapping("/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    /**
     * 返回全量课程列表，按开课时间升序。
     * 支持 ?category=xxx 筛选，前端课程列表页和 AI 客服上下文构建均调用此接口。
     */
    @GetMapping("/list")
    public Result<List<GymCourse>> list(@RequestParam(required = false) String category) {
        LambdaQueryWrapper<GymCourse> query = new LambdaQueryWrapper<>();
        query.eq(StrUtil.isNotBlank(category), GymCourse::getCategory, category);
        query.orderByAsc(GymCourse::getStartTime);
        return Result.success(courseService.list(query));
    }

    /** 管理员新增课程 */
    @PostMapping
    public Result<Void> save(@RequestBody GymCourse course) {
        courseService.save(course);
        return Result.success();
    }

    /** 管理员修改课程信息 */
    @PutMapping
    public Result<Void> update(@RequestBody GymCourse course) {
        courseService.updateById(course);
        return Result.success();
    }

    /** 管理员删除课程，不检查是否有关联预约，调用前应由前端二次确认 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        courseService.removeById(id);
        return Result.success();
    }

    /** 课程详情页调用，返回单条课程完整信息 */
    @GetMapping("/{id}")
    public Result<GymCourse> getById(@PathVariable Long id) {
        return Result.success(courseService.getById(id));
    }

    /** 管理员后台分页查询，支持按课程名称模糊搜索和分类精确筛选 */
    @GetMapping("/page")
    public Result<Page<GymCourse>> findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                           @RequestParam(defaultValue = "10") Integer pageSize,
                           @RequestParam(required = false) String name,
                           @RequestParam(required = false) String category) {
        LambdaQueryWrapper<GymCourse> query = new LambdaQueryWrapper<>();
        query.like(StrUtil.isNotBlank(name), GymCourse::getName, name);
        query.eq(StrUtil.isNotBlank(category), GymCourse::getCategory, category);
        query.orderByAsc(GymCourse::getId);
        return Result.success(courseService.page(new Page<>(pageNum, pageSize), query));
    }
}
