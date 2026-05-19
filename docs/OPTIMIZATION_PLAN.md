# Gym Pro 优化方案

> 本文档基于对 `gym-system/` + `gym-vue/` 的一次完整代码扫描，记录与现状的差距、优化思路、分阶段实施计划。每个阶段独立可发布，相互之间松耦合。

## 0. 背景：文档承诺 vs. 代码现状

| 文档/ADR 承诺 | 代码实际情况 | 影响 |
|---|---|---|
| ADR-0003：BCrypt 密码加密 + 旧明文登录时升级 | `UserServiceImpl.login` 第 35 行 `password.equals(...)` 是**明文对比**，注册 `this.save(user)` 原样落库 | 全表明文密码，安全事故 |
| README：JWT 鉴权 + 路由守卫 | 后端**签发 JWT 但从不校验**，没有任何拦截器/过滤器；Controller 全部从前端 `userId` 取 | 任何登录用户可改 `userId` 操作他人订单 |
| README / CLAUDE.md：`BusinessException` 统一异常体系 | 代码里**不存在** `BusinessException`，全部 `throw new RuntimeException(...)` | 错误码退化为统一 "500"，前端无法区分业务错误 |
| ADR-0004：`BookingStatus` 枚举替代魔法数字 | 代码里**没有枚举**，`status == 0/1/2` 散布在 9 处 | ADR 形同虚设，新人维护易出错 |
| ADR-0005：VO 替代 Map 返回值 | `BookingMapper.selectMyBookings` 仍返 `List<Map<String,Object>>`，业务层还 `instanceof LocalDateTime` 强转 | 类型安全为零 |

## 1. 问题清单（按严重程度）

### 1.1 安全 / 正确性硬伤
- **H1**：密码明文存储 + 明文对比（`UserServiceImpl:35`）
- **H2**：JWT 只签发不校验，`userId` 来自前端参数
- **H3**：`BusinessException` 体系缺失，全 `RuntimeException`
- **H4**：`BookingStatus` 枚举缺失
- **H5**：`selectMyBookings` 返回 Map

### 1.2 并发与一致性
- **C1**：`tryLock(3, 10, SECONDS)` 锁内有 5+ 次 DB 往返，慢查询/GC 卡顿可能在 10s 内释放锁导致超卖
- **C2**：`cancelBooking` 无锁、无状态 CAS，连点两次可双倍回库存
- **C3**：`payBooking` 同样无状态 CAS，并发支付可重复扣款
- **C4**：`buyVip` 先 `getById` 再 `setBalance + updateById`，并发充值会被覆盖
- **C5**：多处 `setSql("balance = balance + " + amount)` 字符串拼接，模式坏
- **C6**：`CorsConfig` `allowedOriginPatterns("*") + allowCredentials(true)` 等于全互联网开放跨域携凭据

### 1.3 设计与可维护性
- **D1**：JWT 密钥硬编码 `"my-secret-key"` 且无过期
- **D2**：登录/注册接口直接收 `SysUser` 实体，前端可注入 `role` / `balance`
- **D3**：VIP 月费/年费写死代码常量
- **D4**：VIP 过期降级只在登录时触发，下单不重查
- **D5**：时间冲突校验是"查全表回内存过滤"
- **D6**：`@Resource` / `@RequiredArgsConstructor` / `@Autowired` 三种注入风格并存
- **D7**：README 承诺的 `src/api/` `src/constants/` 不存在，axios 调用散落 `.vue`
- **D8**：`request.js` 401 不跳登录页，只 toast
- **D9**：`baseURL` 写死 `http://localhost:8080`
- **D10**：AI / 支付宝密钥可能也硬编码（沙箱密钥已确认硬编码在 `application.yml`）

### 1.4 体验 / 性能
- 后端把日期格式化成字符串返前端，丢失排序能力
- `course_booking(user_id, course_id, status)` 等关键索引未确认
- 前端无 Pinia，用户信息靠 `localStorage` 各组件 parse
- `vite.config.js` 空配置，echarts / element-plus 未拆 chunk
- `src/test` 是空骨架，无任何测试

## 2. 分阶段实施计划

### 阶段 0 · 修复"文档承诺但代码没做"（1–2 天）

目标：让代码兑现 README/ADR 描述，关掉两个安全洞，为后续阶段铺类型基础。

| 编号 | 任务 | 文件 |
|---|---|---|
| 0-1 | 新建 `BusinessException` / `UnauthorizedException`，重写 `GlobalExceptionHandler` 分类处理 | `common/exception/*` |
| 0-2 | 新建 `BookingStatus` 枚举（含 `canPay()` / `canCancel()` / `isFinal()`），替换 `BookingServiceImpl` 全部魔法数字 | `entity/enums/BookingStatus.java`、`service/impl/BookingServiceImpl.java` |
| 0-3 | 引入 `BCryptPasswordEncoder`，注册时 hash、登录时 `matches` + 旧明文一次性升级 | `UserServiceImpl.java`、`config/SecurityBeanConfig.java` |
| 0-4 | 加 JWT 拦截器（`HandlerInterceptor`），从 `Authorization` 解析 `uid` 放入 `RequestAttribute`；改 Controller：不再从前端取 `userId` | `config/JwtInterceptor.java`、`config/WebMvcConfig.java`、各 Controller |
| 0-5 | `BookingMapper.selectMyBookings` 改返回 `List<BookingVO>` | `mapper/BookingMapper.java`、`vo/BookingVO.java`、`BookingServiceImpl.java` |

**完成标准**：
- 全表密码逐步迁移为 BCrypt（旧明文用户登录一次后即升级）
- 任意接口未带合法 JWT 时返 401，带了即可拿到当前 `uid`，前端不能伪造他人 userId
- 业务异常返回明确 code（如 `BIZ_BALANCE_NOT_ENOUGH`），系统异常返回 500
- `selectMyBookings` 返回 `BookingVO`，前端类型清晰
- 不破坏现有前端调用（向后兼容：登录接口仍返回 token + user）

### 阶段 1 · 并发与一致性（2–3 天）

| 任务 | 说明 |
|---|---|
| `tryLock` 改看门狗 | `lock.tryLock(3, TimeUnit.SECONDS)`，由 Redisson 自动续期；finally 释放 |
| 状态流转全部 CAS | `payBooking` / `cancelBooking` / `paySuccess` 全部改 `update set status=新 where id=? and status=旧`，行数=0 抛业务异常 |
| `buyVip` 原子化 | `update sys_user set balance=balance-?, vip_type=?, vip_expire_time=? where id=? and balance>=?` 一次性完成 |
| 下单前重查 VIP | `bookCourse` 在计价前调用 `checkVipStatus(user)` |
| `setSql` 参数化 | 用 `setSql("balance = balance + {0}", amount)` 写法 |

### 阶段 2 · 配置外化 + 鉴权完善（1–2 天）

- JWT 密钥、过期时间、支付宝密钥、VIP 价格、AI 密钥统一进 `application.yml`，敏感项支持 `${ENV_VAR}` 占位
- 拆 `application-dev.yml` / `application-prod.yml`
- 登录/注册 DTO 化（`LoginReq`、`RegisterReq`），禁止前端注入 `role` / `balance` / `vipType`
- `CorsConfig.allowedOrigins` 改配置项，默认 `http://localhost:5173`
- 前端 `request.js` 401 自动 `router.replace('/login')`；`baseURL` 读 `import.meta.env.VITE_API_BASE_URL`
- 前端补齐 `src/api/`、`src/constants/`，把视图里散落的 `request.xxx` 收拢
- 后端注入风格统一为构造器注入

### 阶段 3 · 体验、性能、可观测（按需）

- 前端引 Pinia，用户信息走 store
- `vite.config.js` 加 `build.rollupOptions.output.manualChunks`，拆 echarts / element-plus
- 时间冲突校验改 SQL 一次到位
- 日期返 ISO 字符串由前端格式化
- 关键索引补齐 + 配套迁移脚本
- 引 `spring-boot-starter-validation`，DTO 上 `@NotBlank` / `@DecimalMin` 替代 `Hutool.Assert`
- 引 `spring-boot-starter-actuator` + Prometheus 指标（抢课 QPS、锁等待时长、订单状态分布）
- 补集成测试：`Testcontainers` 拉 MySQL+Redis，覆盖 `bookCourse` 并发场景

## 3. 风险与回滚

| 阶段 | 主要风险 | 缓解 |
|---|---|---|
| 0 | BCrypt 改造后旧明文用户登录失败 | 登录时双路径：先 `matches`，失败再判"是否明文"并升级 |
| 0 | JWT 拦截器误拦截 `/auth/**` / `/alipay/notify` | 白名单显式列出，并在拦截器内做路径前缀判断 |
| 1 | CAS update 影响行数=0 误判 | 区分"已是目标状态"和"非法源状态"，分别返不同错误码 |
| 2 | 敏感配置迁移可能漏改 | grep 全仓库 `"my-secret-key"` / `2021000148603415` 等硬编码字符串确保替换干净 |

## 4. 验收

每阶段完成后：
1. 后端 `mvn clean package` 通过
2. 前端 `npm run build` 通过
3. 手工冒烟：登录 → 浏览课程 → 抢课 → 余额支付 → 取消订单 → 充值 → 买 VIP
4. 至少跑一次并发抢课验证（`ab -n 100 -c 20` 或简单 Java 多线程）确认无超卖
