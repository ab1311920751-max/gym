# ADR-0005: Mapper 返回值使用 VO 替代 Map

## 状态

已采纳

## 上下文

`BookingMapper.selectMyBookings()` 返回 `List<Map<String, Object>>`，丧失类型安全，字段名拼写错误无法在编译期发现。

## 决策

定义 `BookingVO` 类替代 `Map<String, Object>` 作为联表查询的返回类型。

## 理由

- 类型安全：字段名错误编译期即可发现
- 可读性：VO 类即文档，一目了然有哪些字段
- 可维护性：修改字段只需改一处
- 前端对接：VO 可直接序列化为 JSON，字段名确定

## 后果

- 需要新增 VO 类
- Mapper XML/注解需指定 resultType 为 VO 类
