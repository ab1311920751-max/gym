# ADR-0002: 折扣策略使用策略模式

## 状态

已采纳

## 上下文

不同 VIP 等级享受不同课程折扣：普通原价、月卡9折、年卡8折。折扣规则可能扩展。

## 决策

使用策略模式（`DiscountStrategy` 接口 + `DiscountFactory` 工厂），通过 `vipType` 映射到对应折扣策略。

## 理由

- 新增折扣类型只需在 Factory 中注册新策略，符合开闭原则
- 折扣逻辑集中在一处，修改不影响调用方
- 策略接口简单（`calculate(BigDecimal) → BigDecimal`），深度足够

## 后果

- 当前实现使用静态 Map 注册策略，未来如需动态策略需改为 Spring Bean 注册
- 折扣计算仅考虑 VIP 等级，未考虑课程类型差异
