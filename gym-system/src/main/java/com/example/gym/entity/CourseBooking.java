package com.example.gym.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 课程预约订单实体，对应数据库表 course_booking。
 * 记录用户预约课程的信息，包含订单号、状态、实际成交价等。
 * 订单状态变更需通过 BookingStatus 枚举判断，不要直接比较数字字面量。
 */
@Data
@TableName("course_booking")
public class CourseBooking {
    /** 主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 预约用户 ID */
    private Long userId;

    /** 预约课程 ID */
    private Long courseId;

    /** 订单号，使用雪花算法生成，全局唯一且不暴露自增规律，用于支付宝支付回调关联 */
    private String bookingNo;

    /** 订单状态：0-PENDING（待支付），1-PAID（已支付），2-CANCELLED（已取消）。对应 BookingStatus 枚举 */
    private Integer status;

    /** 实际成交价，根据用户 VIP 类型计算折扣后固化到订单，防止后续 VIP 变动影响已有订单价格 */
    private BigDecimal realPrice;

    /** 订单创建时间 */
    private LocalDateTime createTime;
}