package com.example.gym.dto;

import lombok.Data;

/**
 * [DTO Pattern] 预约/订单相关参数封装
 */
public class BookingDTO {

    @Data
    public static class CreateReq {
        private Long userId;
        private Long courseId;
    }

    @Data
    public static class CancelReq {
        private Long bookingId;
    }

    @Data
    public static class PayReq {
        private Long bookingId;
    }
}