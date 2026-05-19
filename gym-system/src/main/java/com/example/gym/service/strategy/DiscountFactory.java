package com.example.gym.service.strategy;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * [Design Pattern] Factory Pattern (工厂模式)
 * 负责根据会员类型生产对应的计价策略。
 */
public class DiscountFactory {

    // 0-普通, 1-月卡(9折), 2-年卡(8折)
    private static final Map<Integer, DiscountStrategy> strategies = new HashMap<>();

    static {
        strategies.put(0, price -> price); // 原价
        strategies.put(1, price -> price.multiply(new BigDecimal("0.9"))); // 9折
        strategies.put(2, price -> price.multiply(new BigDecimal("0.8"))); // 8折
    }

    public static BigDecimal calculatePrice(BigDecimal originalPrice, Integer vipType) {
        if (originalPrice == null) return BigDecimal.ZERO;
        // 默认为普通会员策略
        DiscountStrategy strategy = strategies.getOrDefault(vipType, strategies.get(0));
        return strategy.calculate(originalPrice);
    }
}