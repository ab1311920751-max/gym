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

/**
 * 用户管理控制器，包含管理员对用户的 CRUD 操作，
 * 以及用户自助的充值、VIP 购买、资料修改、密码修改等功能。
 * 涉及当前登录用户的操作，uid 均通过 @CurrentUserId 从 JWT 解析，防止前端伪造。
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** 余额充值，uid 来自 JWT，防止给他人充值 */
    @PostMapping("/recharge")
    public Result recharge(@CurrentUserId Long uid,
                           @RequestBody UserDTO.RechargeReq req) {
        userService.recharge(uid, req.getAmount());
        return Result.success();
    }

    /** 购买或续费 VIP，vipType=1 月卡(30元)，vipType=2 年卡(300元) */
    @PostMapping("/buyVip")
    public Result buyVip(@CurrentUserId Long uid,
                         @RequestBody UserDTO.BuyVipReq req) {
        userService.buyVip(uid, req.getVipType());
        return Result.success();
    }

    /** 管理员查询全量用户列表 */
    @GetMapping("/list")
    public Result list() {
        return Result.success(userService.list());
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    /**
     * 管理员新增用户，复用 register 逻辑。
     * service 层强制覆盖 role/balance/vipType，前端传入这些字段无效。
     */
    @PostMapping
    public Result save(@RequestBody UserDTO.RegisterReq req) {
        userService.register(req);
        return Result.success();
    }

    /** 管理员直接更新用户字段，用于后台编辑表单提交 */
    @PutMapping
    public Result update(@RequestBody SysUser user) {
        userService.updateById(user);
        return Result.success();
    }

    /** 用户修改自己的昵称、手机、邮箱、性别等资料（不含密码） */
    @PutMapping("/profile")
    public Result updateProfile(@CurrentUserId Long uid,
                                 @RequestBody UserDTO.UpdateProfileReq req) {
        userService.updateProfile(uid, req);
        return Result.success();
    }

    /** 用户修改密码，需先验证原密码 */
    @PutMapping("/password")
    public Result changePassword(@CurrentUserId Long uid,
                                  @RequestBody UserDTO.ChangePasswordReq req) {
        userService.changePassword(uid, req.getOldPassword(), req.getNewPassword());
        return Result.success();
    }

    /** 管理员删除用户 */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        userService.removeById(id);
        return Result.success();
    }

    /**
     * 启用/禁用账号（status=1 启用，status=0 禁用）。
     * operatorId 从 JWT 取，service 层会阻止管理员禁用自己。
     */
    @PutMapping("/{id}/status")
    public Result updateStatus(@CurrentUserId Long operatorId,
                               @PathVariable Long id,
                               @RequestParam Integer status) {
        userService.updateStatus(operatorId, id, status);
        return Result.success();
    }

    /** 分页查询用户列表，支持按用户名模糊搜索，管理员后台使用 */
    @GetMapping("/page")
    public Result findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                           @RequestParam(defaultValue = "10") Integer pageSize,
                           @RequestParam(required = false) String username) {
        LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<>();
        query.like(StrUtil.isNotBlank(username), SysUser::getUsername, username);
        query.orderByAsc(SysUser::getId);
        return Result.success(userService.page(new Page<>(pageNum, pageSize), query));
    }
}
