# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 仓库布局

这是一个双模块的全栈项目，两个子目录是**独立的 git 仓库**（各自包含 `.git`），根目录本身也是一个 git 仓库。在 `gym-system/` 或 `gym-vue/` 中运行 git 命令时，操作的是子仓库而非顶层仓库。

- `gym-system/` — Spring Boot 3 后端（Maven）
- `gym-vue/` — Vue 3 前端（Vite）
- `docs/adr/` — 架构决策记录（ADR 0001–0005），是判断"为何这样设计"的权威依据
- `CONTEXT.md` — 领域语言定义（User/Course/Booking/Wallet/VIP/Discount 术语 + 订单状态机）

## 常用命令

### 后端（在 `gym-system/` 下）

```bash
mvn spring-boot:run        # 启动后端（默认 8080）
mvn clean package          # 打包
mvn test                   # 运行测试（注意：src/test 目录目前为空骨架，无实际测试用例）
```

### 前端（在 `gym-vue/` 下）

```bash
npm install
npm run dev      # 开发服务器（默认 5173）
npm run build    # 生产构建（输出到 dist/）
npm run preview  # 预览生产构建
```

### 数据库初始化

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS gym_db DEFAULT CHARACTER SET utf8mb4;"
mysql -u root -p gym_db < gym-vue/db_backups/gym_db4.sql
```

> README 中提到的 `migrate_v2.sql` 在当前仓库中并不存在；`gym_db4.sql` 已经是合并版的最新结构。如果用户提到迁移脚本缺失，按此说明即可，不要凭空创建。
> 
> 根 README 引用了 `gym-system/README.md`，但该文件**实际不存在**。

### 运行依赖

- MySQL 8.0（`application.yml` 默认 `localhost:3306/gym_db`，用户 `root`/`123456`）
- Redis 6.0+（Redisson 默认连 `localhost:6379`，**抢课接口若 Redis 未启动会直接失败**）
- JDK 17、Node 18+

### 测试账号

| 账号 | 密码 | 角色 |
|------|------|------|
| admin | 123456 | 管理员 |
| cd | 123456 | 普通用户（月卡 VIP） |
| 陈东 | 123456 | 普通用户（年卡 VIP） |

## 后端架构

### 分层与包结构（`com.example.gym.*`）

```
Controller → Service(impl) → Mapper → MySQL
              ↓
           strategy/         （DiscountFactory + DiscountStrategy）
DTO（入参）  Entity（表映射）  VO（响应）  Enum（状态/类型）
common/      → Result<T>、GlobalExceptionHandler
config/      → AliPayConfig、CorsConfig
```

- Controller 一律返回 `Result<T>`；异常通过 `GlobalExceptionHandler` 统一转成 `Result.fail(...)`。
- Service 接口在 `service/`，实现在 `service/impl/`。新增业务时遵循"接口 + 实现"分离的现有约定。
- 折扣计算走策略模式：`DiscountFactory.get(vipType).apply(price)`。新增 VIP 等级时只需要新增 `DiscountStrategy` 实现并在工厂注册，**不要**在业务代码里写 `if (vipType == ...)` 分支（参见 ADR-0002）。
- 业务异常用 `BusinessException` / `UnauthorizedException` 抛出，不要返回错误码 Map。

### 高并发抢课（核心路径）

`BookingServiceImpl` 中预约课程时使用 Redisson 分布式锁，锁键格式 `gym:booking:lock:{courseId}`，`tryLock(3, 10, SECONDS)`。临界区内部完成：库存校验 → 重复/时间冲突校验 → 原子扣减库存 → 折扣计算 → 写订单。修改这段代码时要保持：
1. 所有"读+判断+写"必须在锁内完成；
2. 锁的粒度保持 courseId 级别，不要降级为全局锁或类锁；
3. 抛异常时 try/finally 仍要释放锁（参见 ADR-0001）。

### 订单状态机

`BookingStatus` 枚举：`PENDING(0) → PAID(1) → CANCELLED(2)`。判断状态用 `canPay()` / `canCancel()` 等方法，**不要直接比较数字字面量 0/1/2**（参见 ADR-0004）。

### 密码

注册和密码修改用 BCrypt 哈希。登录时若发现库里存的是明文（旧数据），验证通过后**就地升级**为 BCrypt 哈希再写回（ADR-0003）。引入新的认证流程时要保留这条向后兼容路径。

### 鉴权

使用 Hutool 的 JWT（`cn.hutool.jwt`）签发与校验。前端把 token 存 `localStorage` 并由 Axios 拦截器加到请求头。Spring Security 依赖**只用作 BCrypt**，没有配置 SecurityFilterChain；不要假设有 Security 的过滤链在工作。

鉴权通过 `JwtInterceptor`（`HandlerInterceptor`）实现，从 `Authorization` 请求头解析 JWT，将 `uid` 和 `role` 写入 `RequestAttribute`。新增 Controller 方法时，通过 `@CurrentUserId Long userId` 参数注解获取当前用户 ID——**不要**从请求体或 URL 参数中取 `userId`，否则会被伪造。

### 支付宝

沙箱配置在 `application.yml` 的 `alipay.*`（appId、应用私钥、支付宝公钥、return-url、notify-url 都已写死）。本地无法收到 `notify-url` 回调，依赖 `return-url` 同步跳转 + 主动查询订单状态。

## 前端架构

> `src/components/` 目录当前为空（`HelloWorld.vue` 脚手架代码已删除）。

### 路由与权限

`src/router/index.js` 用 Vue Router 4，所有受保护页面挂在 `Layout` 下作为子路由。`beforeEach` 守卫（具体实现见文件，此处只列规则）：
1. `/login` 白名单，直接放行；
2. `localStorage.token` 和 `localStorage.user` **双重校验**——任一缺失或 user 无有效 id 即清除残留数据并跳登录；
3. `/admin-*` 仅 `role === 'admin'` 可访问，否则 `ElMessage.error` + 跳首页；
4. 后端 `JwtInterceptor` + 前端 Axios 401 拦截器作为第二道防线，token 过期时由后端 401 触发 `redirectToLogin()`。

新增管理员页面时，命名必须以 `admin-` 前缀，否则会绕过权限。新增受保护页面要挂在 `Layout` 子路由里。

### API 调用

所有 HTTP 调用经过 `src/utils/request.js`（Axios 实例 + token 注入 + 错误拦截）。API 调用已收拢到 `src/api/`（按模块拆分，如 `auth.js`、`booking.js`、`course.js`），常量定义在 `src/constants/`（如 `booking.js`、`vip.js`、`role.js`）。新增页面时遵循此约定，不要把 axios 调用零散写进 `.vue`。

`request.js` 响应拦截器中 `res.code === '200'` 是**字符串**比较（不是数字），定义新接口时 code 字段务必返回字符串 `"200"`。401 时清除 localStorage 并跳 `/login`。

### 数据可视化

数据驾驶舱（首页/管理首页）使用 ECharts 6。注意 ECharts 6 与 5 的 API 有少量差异，参考已有图表实现。

### AI 集成（双通道）

项目中存在**两套独立的 AI 对话**，共存但互不干扰：

1. **Coze SDK 浮窗**（`index.html`）：第三方 AI 聊天机器人，通过 CDN 加载 Coze Web SDK，bot_id 和 PAT token 硬编码在 `<script>` 标签中。浮窗按钮出现在所有页面（包括登录页），是一个完全独立的外部 AI 通道。

2. **AiChat.vue 组件**（`src/components/AiChat.vue`）：自建 AI 对话组件，在 `Layout.vue` 中渲染（仅 Layout 包裹的页面可见）。固定定位的紫色浮动球，展开为 360×520px 聊天窗口，调用后端 `POST /ai/chat` 接口。后端 `AiServiceImpl` 是**本地规则匹配**（Demo Mode），通过关键词匹配（推荐/余额/VIP）返回预设回复，非真实 LLM 调用。预留了 `callDeepSeekApi` 扩展点。

### 状态管理

当前**无 Pinia/Vuex**，用户状态靠 `localStorage` 直接读写。各组件自行 `JSON.parse(localStorage.getItem('user'))`，无统一 store。路由守卫也直接读 `localStorage.user`。

## ADR 是设计依据

修改下列任何主题前，先读对应 ADR——它们是"为什么这样做"的来源，提交合并请求时如要偏离需要说明理由：

| ADR | 主题 |
|-----|------|
| 0001 | 分布式锁选型（Redisson + courseId 粒度） |
| 0002 | 折扣策略（策略 + 工厂模式） |
| 0003 | 密码存储（BCrypt + 旧明文就地升级） |
| 0004 | 订单状态（枚举替代数字） |
| 0005 | Mapper 返回值（VO 替代 Map） |

> `docs/OPTIMIZATION_PLAN.md` 记录了文档承诺与代码现状的差距及分阶段改进计划，是理解项目技术债务和待办事项的重要参考。

## 调整代码时的隐性约定

- Mapper 返回业务对象时一律用 VO，不要用 `Map<String, Object>`（ADR-0005）。
- Controller 永远返回 `Result<T>`，不要直接抛 `ResponseEntity` 或裸 Map。
- 新增业务异常优先继承 `BusinessException`，并附带可被前端识别的错误码。
- 新增枚举/状态时把"可读 API"（如 `canCancel()`、`isExpired()`）一起加上，避免下游写散开的 if/switch。
- 前后端默认端口是 8080 / 5173；CORS 在 `CorsConfig` 中配置，调整端口需同步两端。
