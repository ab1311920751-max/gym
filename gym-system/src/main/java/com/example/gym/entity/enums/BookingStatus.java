package com.example.gym.entity.enums;

public enum BookingStatus {

    PENDING(0, "待支付"),
    PAID(1, "已支付"),
    CANCELLED(2, "已取消");

    private final Integer code;
    private final String desc;

    BookingStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public boolean canPay() {
        return this == PENDING;
    }

    public boolean canCancel() {
        return this == PENDING || this == PAID;
    }

    public boolean isFinal() {
        return this == CANCELLED;
    }

    public static BookingStatus of(Integer code) {
        if (code == null) {
            return null;
        }
        for (BookingStatus s : values()) {
            if (s.code.equals(code)) {
                return s;
            }
        }
        throw new IllegalArgumentException("未知订单状态: " + code);
    }
}
