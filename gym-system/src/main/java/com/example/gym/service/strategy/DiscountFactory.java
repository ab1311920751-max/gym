package com.example.gym.service.strategy;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 折扣工厂，维护 vipType → DiscountStrategy 的映射。
 * 调用方只需 DiscountFactory.calculatePrice(price, vipType)，无需关心折扣率。
 * 新增 VIP 等级：在 static 块加一行 strategies.put(newType, price -> ...)，其他代码不用改。
 */
public class DiscountFactory {

    // vipType: 0=普通（原价）, 1=月卡（9折）, 2=年卡（8折）
    private static final Map<Integer, DiscountStrategy> strategies = new HashMap<>();

    static {
        strategies.put(0, price -> price);
        strategies.put(1, price -> price.multiply(new BigDecimal("0.9")));
        strategies.put(2, price -> price.multiply(new BigDecimal("0.8")));
    }

    public static BigDecimal calculatePrice(BigDecimal originalPrice, Integer vipType) {
        if (originalPrice == null) return BigDecimal.ZERO;
        // vipType 不在已知类型中时降级为普通策略（原价），避免空指针
        DiscountStrategy strategy = strategies.getOrDefault(vipType, strategies.get(0));
        return strategy.calculate(originalPrice);
    }
}
