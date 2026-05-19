package com.example.gym.service.strategy;

import java.math.BigDecimal;

/**
 * [Design Pattern] Strategy Pattern (策略模式)
 * 定义会员折扣计算的通用接口。
 * 作用：解耦具体的价格计算逻辑，方便后续扩展（如增加“黑金会员”）。
 */
public interface DiscountStrategy {
    BigDecimal calculate(BigDecimal originalPrice);
}