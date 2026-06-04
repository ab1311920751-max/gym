package com.example.gym.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 系统用户实体，对应数据库表 sys_user。
 * 包含用户基本信息、角色、余额、VIP 状态等字段。
 * 密码使用 BCrypt 加密存储，兼容历史明文密码（登录时自动升级）。
 */
@Data
@TableName("sys_user")
public class SysUser {
    /** 主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名，唯一，用于登录 */
    private String username;

    /** 密码，新用户注册时使用 BCrypt 加密，历史数据兼容明文（登录时自动升级） */
    private String password;

    /** 角色："admin" 管理员，"user" 普通用户。注册时由后端强制赋值为 "user"，防止前端角色注入 */
    private String role;

    /** 账户余额，精度为 BigDecimal。充值使用 SQL 原子累加，支付使用 ge 条件防超扣 */
    private BigDecimal balance;

    /** VIP 类型：0-普通会员，1-月卡（30元/月，享9折），2-年卡（300元/年，享8折） */
    private Integer vipType;

    /** VIP 到期时间，登录时自动检查过期并降为普通会员 */
    private LocalDateTime vipExpireTime;

    /** 注册时间 */
    private LocalDateTime createTime;

    /** 昵称 */
    private String nickname;

    /** 手机号 */
    private String phone;

    /** 性别：0-未知，1-男，2-女 */
    private Integer gender;

    /** 邮箱 */
    private String email;

    /** 账号状态：0-禁用，1-正常。管理员不能禁用自己 */
    private Integer status;
}