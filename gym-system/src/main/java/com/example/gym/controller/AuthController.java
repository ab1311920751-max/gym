package com.example.gym.controller;

import com.example.gym.common.Result;
import com.example.gym.dto.UserDTO;
import com.example.gym.entity.SysUser;
import com.example.gym.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public Result login(@RequestBody SysUser user) {
        Map<String, Object> data = userService.login(user);
        return Result.success(data);
    }

    @PostMapping("/register")
    public Result register(@RequestBody UserDTO.RegisterReq req) {
        userService.register(req);
        return Result.success();
    }
}
