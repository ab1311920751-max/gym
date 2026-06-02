package com.example.gym.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.gym.common.Result;
import com.example.gym.entity.Banner;
import com.example.gym.service.BannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/banner")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    @GetMapping("/list")
    public Result list() {
        LambdaQueryWrapper<Banner> query = new LambdaQueryWrapper<>();
        query.eq(Banner::getStatus, 1);
        query.orderByAsc(Banner::getSort);
        return Result.success(bannerService.list(query));
    }

    @GetMapping("/page")
    public Result findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                           @RequestParam(defaultValue = "10") Integer pageSize,
                           @RequestParam(required = false) String title) {
        LambdaQueryWrapper<Banner> query = new LambdaQueryWrapper<>();
        query.like(StrUtil.isNotBlank(title), Banner::getTitle, title);
        query.orderByAsc(Banner::getSort);
        return Result.success(bannerService.page(new Page<>(pageNum, pageSize), query));
    }

    @PostMapping
    public Result save(@RequestBody Banner banner) {
        bannerService.save(banner);
        return Result.success();
    }

    @PutMapping
    public Result update(@RequestBody Banner banner) {
        bannerService.updateById(banner);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        bannerService.removeById(id);
        return Result.success();
    }
}
