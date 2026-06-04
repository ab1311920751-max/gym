package com.example.gym.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;
    private String password;
    private String role; // "admin" 或 "user"

    private BigDecimal balance; // 余额

    // 0-普通, 1-月卡, 2-年卡
    private Integer vipType;

    // [Business Logic] 新增：VIP 到期时间
    private LocalDateTime vipExpireTime;

    private LocalDateTime createTime;

    private String nickname;

    private String phone;

    private Integer gender;

    private String email;

    // 0=禁用，1=正常
    private Integer status;
}