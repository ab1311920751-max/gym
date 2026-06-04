package com.example.gym.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * "我的订单"列表项视图对象。
 * 字段与 BookingMapper.selectMyBookings 的列别名一一对应。
 * startTime 保留 LocalDateTime 以便业务层做时间判断；
 * 序列化给前端前由 Service 层格式化为字符串。
 */
@Data
public class BookingVO {
    private Long id;
    private Integer status;
    private LocalDateTime bookingTime;
    private String courseName;
    private String coach;
    private LocalDateTime startTime;
    private BigDecimal price;
    private BigDecimal realPrice;

    /** 给前端展示的已格式化时间。Service 层填充。 */
    private String startTimeDisplay;
}
