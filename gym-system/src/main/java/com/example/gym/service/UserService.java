package com.example.gym.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.gym.dto.UserDTO;
import com.example.gym.entity.SysUser;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 用户业务接口，继承 MyBatis-Plus IService 获得通用 CRUD 能力，
 * 在此基础上扩展登录、注册、充值、VIP 购买等业务方法。
 */
public interface UserService extends IService<SysUser> {

    /**
     * 登录校验，兼容 BCrypt 哈希与历史明文密码。
     * 校验通过后签发 JWT，返回 token 和用户信息。
     * 若检测到明文密码，登录时同步升级为 BCrypt（无感迁移）。
     */
    Map<String, Object> login(SysUser user);

    /**
     * 注册新用户，role/balance/vipType 在方法内强制赋默认值，
     * 前端传入这些字段无效，防止权限注入。
     */
    void register(UserDTO.RegisterReq req);

    /**
     * 余额充值，使用 SQL 原子累加（balance = balance + amount），
     * userId 来自 JWT，防止给他人充值。
     */
    void recharge(Long userId, BigDecimal amount);

    /**
     * 购买或续费 VIP（vipType=1 月卡 30元，vipType=2 年卡 300元）。
     * 若当前 VIP 未过期，在原到期时间上叠加时长（续费不损失剩余时间）；
     * 否则从当前时间重新开始计算。
     */
    void buyVip(Long userId, Integer vipType);

    /** 修改个人资料（昵称、手机、邮箱、性别），变更用户名时检查唯一性 */
    void updateProfile(Long userId, UserDTO.UpdateProfileReq req);

    /** 修改密码，需验证原密码后才允许更新 */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 启用/禁用用户账号（status=1 启用，status=0 禁用）。
     * operatorId 用于防止管理员禁用自己。
     */
    void updateStatus(Long operatorId, Long targetId, Integer status);
}
