package com.example.gym.controller;

import com.example.gym.common.Result;
import com.example.gym.dto.UserDTO;
import com.example.gym.entity.SysUser;
import com.example.gym.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器，处理登录与注册。
 * 这两个接口在 WebConfig 中配置为白名单，不经过 JwtInterceptor 校验。
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /** 登录成功返回 JWT token 和用户基本信息，token 由前端存入 localStorage */
    @PostMapping("/login")
    public Result login(@RequestBody SysUser user) {
        Map<String, Object> data = userService.login(user);
        return Result.success(data);
    }

    /** 注册成功后不自动登录，需用户手动跳转登录页 */
    @PostMapping("/register")
    public Result register(@RequestBody UserDTO.RegisterReq req) {
        userService.register(req);
        return Result.success();
    }
}
