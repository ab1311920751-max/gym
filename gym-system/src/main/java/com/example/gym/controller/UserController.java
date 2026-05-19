package com.example.gym.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.gym.common.Result;
import com.example.gym.common.auth.CurrentUserId;
import com.example.gym.dto.UserDTO;
import com.example.gym.entity.SysUser;
import com.example.gym.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/recharge")
    public Result recharge(@CurrentUserId Long uid,
                           @RequestBody UserDTO.RechargeReq req) {
        userService.recharge(uid, req.getAmount());
        return Result.success();
    }

    @PostMapping("/buyVip")
    public Result buyVip(@CurrentUserId Long uid,
                         @RequestBody UserDTO.BuyVipReq req) {
        userService.buyVip(uid, req.getVipType());
        return Result.success();
    }

    @GetMapping("/list")
    public Result list() {
        return Result.success(userService.list());
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    /**
     * 管理员新增用户。register 内部会强制覆盖 role/balance/vipType，前端注入无效。
     */
    @PostMapping
    public Result save(@RequestBody SysUser user) {
        userService.register(user);
        return Result.success();
    }

    @PutMapping
    public Result update(@RequestBody SysUser user) {
        userService.updateById(user);
        return Result.success();
    }

    @PutMapping("/profile")
    public Result updateProfile(@CurrentUserId Long uid,
                                 @RequestBody UserDTO.UpdateProfileReq req) {
        userService.updateProfile(uid, req.getUsername());
        return Result.success();
    }

    @PutMapping("/password")
    public Result changePassword(@CurrentUserId Long uid,
                                  @RequestBody UserDTO.ChangePasswordReq req) {
        userService.changePassword(uid, req.getOldPassword(), req.getNewPassword());
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        userService.removeById(id);
        return Result.success();
    }

    @GetMapping("/page")
    public Result findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                           @RequestParam(defaultValue = "10") Integer pageSize,
                           @RequestParam(required = false) String username) {
        LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<>();
        query.like(StrUtil.isNotBlank(username), SysUser::getUsername, username);
        query.orderByDesc(SysUser::getId);
        return Result.success(userService.page(new Page<>(pageNum, pageSize), query));
    }
}
