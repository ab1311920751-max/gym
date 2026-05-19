package com.example.gym.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("course_booking")
public class CourseBooking {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long courseId;

    // --- 新增业务字段 ---
    private String bookingNo;   // 订单号
    private Integer status;     // 0-待支付, 1-已成功, 2-已取消
    private BigDecimal realPrice; // 实际成交价
    private LocalDateTime createTime;
}