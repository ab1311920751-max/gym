package com.example.gym.service.strategy;

import java.math.BigDecimal;

/**
 * 折扣计算策略接口（策略模式）。
 * 每种 VIP 类型实现一个策略，新增等级只需加实现类并在 DiscountFactory 注册，
 * 调用方不感知具体折扣逻辑。
 */
public interface DiscountStrategy {
    BigDecimal calculate(BigDecimal originalPrice);
}
