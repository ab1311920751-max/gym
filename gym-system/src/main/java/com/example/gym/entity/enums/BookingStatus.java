package com.example.gym.entity.enums;

/**
 * 预约订单状态枚举，定义三种状态及其转换规则。
 * <p>
 * 状态列表：
 * <ul>
 *   <li>PENDING(0, "待支付") — 刚创建，未支付</li>
 *   <li>PAID(1, "已支付") — 支付完成</li>
 *   <li>CANCELLED(2, "已取消") — 终态，不可再变更</li>
 * </ul>
 * <p>
 * 状态转换：
 * <ul>
 *   <li>PENDING → PAY：canPay() = true</li>
 *   <li>PENDING → CANCEL：canCancel() = true</li>
 *   <li>PAID → CANCEL：canCancel() = true（退款 + 回库存）</li>
 *   <li>CANCELLED → 任何操作：不允许（isFinal() = true）</li>
 * </ul>
 * <p>
 * 判断状态请使用 canPay()/canCancel()/isFinal() 方法，不要直接比较数字字面量 0/1/2。
 */
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

    /** 仅 PENDING 状态可支付 */
    public boolean canPay() {
        return this == PENDING;
    }

    /** PENDING 和 PAID 可取消（PAID 取消会退款+回库存） */
    public boolean canCancel() {
        return this == PENDING || this == PAID;
    }

    /** CANCELLED 为终态，不可再变更 */
    public boolean isFinal() {
        return this == CANCELLED;
    }

    /** 根据 code 值查找枚举，用于从数据库 status 字段反序列化 */
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
