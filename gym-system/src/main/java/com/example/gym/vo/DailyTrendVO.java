package com.example.gym.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 每日订单趋势数据，与 BookingMapper.selectDailyTrend 的列别名对应。
 */
@Data
public class DailyTrendVO {
    /** 日期，格式 MM-dd */
    private String date;
    /** 当日已支付订单数 */
    private int bookingCount;
    /** 当日已支付营收 */
    private BigDecimal revenue;
}
