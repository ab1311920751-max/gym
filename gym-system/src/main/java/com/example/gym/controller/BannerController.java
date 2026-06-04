package com.example.gym.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.gym.common.Result;
import com.example.gym.entity.Banner;
import com.example.gym.service.BannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 轮播图控制器。
 * GET /list 只返回 status=1（启用）的 Banner，按 sort 升序，供首页展示；
 * GET /page 返回全量数据（含禁用），供管理员后台管理使用。
 */
@RestController
@RequestMapping("/banner")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    /** 首页轮播图接口，只返回已启用且按 sort 排序的 Banner */
    @GetMapping("/list")
    public Result<java.util.List<Banner>> list() {
        LambdaQueryWrapper<Banner> query = new LambdaQueryWrapper<>();
        query.eq(Banner::getStatus, 1);
        query.orderByAsc(Banner::getSort);
        return Result.success(bannerService.list(query));
    }

    /** 管理员后台分页查询，支持按标题模糊搜索，包含已禁用的 Banner */
    @GetMapping("/page")
    public Result<Page<Banner>> findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                           @RequestParam(defaultValue = "10") Integer pageSize,
                           @RequestParam(required = false) String title) {
        LambdaQueryWrapper<Banner> query = new LambdaQueryWrapper<>();
        query.like(StrUtil.isNotBlank(title), Banner::getTitle, title);
        query.orderByAsc(Banner::getSort);
        return Result.success(bannerService.page(new Page<>(pageNum, pageSize), query));
    }

    /** 管理员新增轮播图 */
    @PostMapping
    public Result<Void> save(@RequestBody Banner banner) {
        bannerService.save(banner);
        return Result.success();
    }

    /** 管理员修改轮播图（含启用/禁用切换） */
    @PutMapping
    public Result<Void> update(@RequestBody Banner banner) {
        bannerService.updateById(banner);
        return Result.success();
    }

    /** 管理员删除轮播图 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        bannerService.removeById(id);
        return Result.success();
    }
}
