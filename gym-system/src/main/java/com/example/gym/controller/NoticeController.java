package com.example.gym.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.gym.common.Result;
import com.example.gym.entity.SysNotice;
import com.example.gym.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    /** 用户端：只返回已发布的公告，按创建时间倒序 */
    @GetMapping("/list")
    public Result<List<SysNotice>> list() {
        LambdaQueryWrapper<SysNotice> query = new LambdaQueryWrapper<>();
        query.eq(SysNotice::getStatus, 1);
        query.orderByDesc(SysNotice::getCreateTime);
        return Result.success(noticeService.list(query));
    }

    /** 管理后台：分页查询，支持标题模糊搜索和状态筛选 */
    @GetMapping("/page")
    public Result<Page<SysNotice>> findPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<SysNotice> query = new LambdaQueryWrapper<>();
        query.like(StrUtil.isNotBlank(title), SysNotice::getTitle, title);
        query.eq(status != null, SysNotice::getStatus, status);
        query.orderByDesc(SysNotice::getCreateTime);
        return Result.success(noticeService.page(new Page<>(pageNum, pageSize), query));
    }

    @PostMapping
    public Result<Void> save(@RequestBody SysNotice notice) {
        noticeService.save(notice);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody SysNotice notice) {
        noticeService.updateById(notice);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        noticeService.removeById(id);
        return Result.success();
    }
}
