package com.example.gym.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.gym.dto.UserDTO;
import com.example.gym.entity.SysUser;
import java.math.BigDecimal;
import java.util.Map;

public interface UserService extends IService<SysUser> {
    Map<String, Object> login(SysUser user);
    void register(UserDTO.RegisterReq req);

    /**
     * 充值余额
     * @param userId 用户ID
     * @param amount 金额
     */
    void recharge(Long userId, BigDecimal amount);

    /**
     * 购买VIP
     * @param userId 用户ID
     * @param vipType 1-月卡(30元), 2-年卡(300元)
     */
    void buyVip(Long userId, Integer vipType);

    void updateProfile(Long userId, UserDTO.UpdateProfileReq req);

    void changePassword(Long userId, String oldPassword, String newPassword);
}