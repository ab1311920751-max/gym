package com.example.gym.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * [DTO Pattern] 数据传输对象
 * 用于规范前端传入的参数，避免使用 Map<String, Object> 导致的类型转换异常。
 */
public class UserDTO {

    @Data
    public static class RechargeReq {
        private Long userId;
        private BigDecimal amount;
    }

    @Data
    public static class BuyVipReq {
        private Long userId;
        private Integer vipType; // 1-月卡, 2-年卡
    }

    @Data
    public static class LoginReq {
        private String username;
        private String password;
    }

    @Data
    public static class UpdateProfileReq {
        private String username;
    }

    @Data
    public static class ChangePasswordReq {
        private String oldPassword;
        private String newPassword;
    }
}