# ADR-0003: 密码存储使用 BCrypt 哈希

## 状态

已采纳

## 上下文

MVP 阶段密码以明文存储在 `sys_user.password` 字段中，存在严重安全隐患。

## 决策

使用 Spring Security 的 `BCryptPasswordEncoder` 对密码进行哈希存储。登录时使用 `matches()` 方法验证。

## 理由

- BCrypt 自带盐值，无需额外管理
- 计算成本可调（默认10轮），抗暴力破解
- Spring Security 生态原生支持
- 迁移方案：对已有明文密码，首次登录成功后自动升级为 BCrypt 哈希

## 后果

- 需要引入 `spring-boot-starter-security` 依赖（仅使用其 BCrypt 功能）
- 已有用户数据需执行一次性迁移脚本
- 密码字段长度需从 varchar(100) 扩展至 varchar(255)
