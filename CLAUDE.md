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
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS gym_db1 DEFAULT CHARACTER SET utf8mb4;"
mysql -u root -p gym_db1 < gym-vue/db_backups/gym_db4.sql
```

> README 中提到的 `migrate_v2.sql` 在当前仓库中并不存在；`gym_db4.sql` 已经是合并版的最新结构。如果用户提到迁移脚本缺失，按此说明即可，不要凭空创建。
>
> 根 README 引用了 `gym-system/README.md`，但该文件**实际不存在**。
>
> AI 客服所需的 `ai_chat_session` 和 `ai_chat_message` 两张表已在 `gym_db1` 中存在，无需重新建表。迁移脚本位于 `gym-vue/db_backups/migrate_ai.sql`（仅供参考）。

### 运行依赖

- MySQL 8.0（`application.yml` 默认 `localhost:3306/gym_db1`，用户 `root`/`123456`）
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
common/      → Result<T>、GlobalExceptionHandler、ErrorCode
config/      → AliPayConfig、CorsConfig
```

- Controller 一律返回 `Result<T>`；异常通过 `GlobalExceptionHandler` 统一转成 `Result.fail(...)`。
- Service 接口在 `service/`，实现在 `service/impl/`。新增业务时遵循"接口 + 实现"分离的现有约定。
- 折扣计算走策略模式：`DiscountFactory.get(vipType).apply(price)`。新增 VIP 等级时只需要新增 `DiscountStrategy` 实现并在工厂注册，**不要**在业务代码里写 `if (vipType == ...)` 分支（参见 ADR-0002）。
- 业务异常用 `BusinessException` / `UnauthorizedException` 抛出，不要返回错误码 Map。

### 主要 Controller 与端点

| Controller | 前缀 | 主要端点 |
|---|---|---|
| `AuthController` | `/auth` | `POST /login`、`POST /register` |
| `UserController` | `/user` | `GET /page`、`GET /{id}`、`POST`、`PUT`、`PUT /profile`、`PUT /password`、`PUT /{id}/status`、`DELETE /{id}`、`POST /recharge`、`POST /buyVip` |
| `CourseController` | `/course` | `GET /list`（支持 `?category=` 筛选）、`GET /page`（支持名称/分类筛选）、`GET /{id}`、`POST`、`PUT`、`DELETE /{id}` |
| `BookingController` | `/booking` | `POST /create`、`GET /my`、`POST /cancel/{id}`、`POST /pay/{id}` |
| `AlipayController` | `/alipay` | `GET /pay`（课程/充值双模式）、`GET /return`（课程支付回调）、`POST /success`（充值回调） |
| `BannerController` | `/banner` | `GET /list`（仅 status=1）、`GET /page`、`POST`、`PUT`、`DELETE /{id}` |
| `ReportController` | `/report` | `GET /dashboard`（用户数、订单数、总收入、VIP 分布） |
| `AiController` | `/ai` | `POST /chat`、`POST /chat/stream`（SSE）、`GET /sessions`、`GET /sessions/{id}/messages`、`DELETE /sessions/{id}` |

### 主要实体字段

**SysUser**（表 `sys_user`）
- `id`、`username`、`password`、`role`（"admin"/"user"）
- `balance`（余额）、`vipType`（0=普通，1=月卡，2=年卡）、`vipExpireTime`
- `nickname`、`phone`、`gender`、`email`
- `status`（0=禁用，1=正常）、`createTime`

**GymCourse**（表 `gym_course`）
- `id`、`name`、`coach`、`description`、`content`（详细内容）
- `category`（课程分类，对应 `COURSE_CATEGORIES` 常量）
- `startTime`（`LocalDateTime`，对应数据库 `start_time` 字段）
- `capacity`（最大人数）、`stock`（库存）、`price`
- `version`（乐观锁字段，`@TableField(fill = FieldFill.INSERT)`；`@Version` 注解当前注释掉）

### 错误码（`ErrorCode` 枚举）

| 范围 | 含义 |
|---|---|
| 200 | 成功 |
| 400 | 参数/业务异常（通用） |
| 401 | 未登录或 token 过期 |
| 403 | 无权访问 |
| 404 | 资源不存在 |
| 4001–4006 | 用户相关（不存在、密码错、用户名重复、原密码错、账号禁用、不能禁用自己） |
| 4101 | 余额不足 |
| 4201–4205 | 课程/预约相关（不存在、售罄、已过期、重复预约、时间冲突） |
| 4301–4302 | 订单相关（不存在、状态非法） |
| 4901 | 分布式锁超时（抢购人数过多） |
| 500 | 系统错误 |

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

> `src/components/` 目录当前为空（脚手架代码已删除）。

### 页面视图（`src/views/`，共 13 个）

| 文件 | 路由路径 | 说明 |
|---|---|---|
| `Login.vue` | `/login` | 登录页（白名单，未登录可访问） |
| `Layout.vue` | `/`（壳） | 主布局（侧边栏 + 顶栏） |
| `Home.vue` | `/home` | 数据驾驶舱（ECharts 图表） |
| `Course.vue` | `/course` | 课程列表（支持分类/状态/排序筛选） |
| `CourseDetail.vue` | `/course/:id` | 课程详情页 |
| `MyBooking.vue` | `/my-booking` | 我的预约 |
| `Wallet.vue` | `/wallet` | 钱包/充值/购买 VIP |
| `Profile.vue` | `/profile` | 个人资料修改 |
| `AiChat.vue` | `/ai-chat` | AI 对话（会话列表 + SSE 流式） |
| `PaySuccess.vue` | `/pay/success` | 支付宝同步回调落地页 |
| `UserHome.vue` | — | 用户首页子组件（由 Home.vue 引用） |
| `AdminCourse.vue` | `/admin-course` | 课程管理（仅管理员） |
| `AdminUser.vue` | `/admin-user` | 用户管理（仅管理员） |

### 路由与权限

`src/router/index.js` 用 Vue Router 4，所有受保护页面挂在 `Layout` 下作为子路由。`beforeEach` 守卫规则：
1. `/login` 白名单，直接放行；
2. `localStorage.token` 和 `localStorage.user` **双重校验**——任一缺失或 user 无有效 id 即清除残留数据并跳登录；
3. **Token 过期检查**——解析 JWT payload 的 `exp` 字段，过期则清除并跳登录（`ElMessage.warning`）；
4. `/admin-*` 仅 `role === 'admin'` 可访问，否则 `ElMessage.error` + 跳首页；
5. 后端 `JwtInterceptor` + 前端 Axios 401 拦截器作为第二道防线，token 过期时由后端 401 触发 `redirectToLogin()`。

新增管理员页面时，命名必须以 `admin-` 前缀，否则会绕过权限。新增受保护页面要挂在 `Layout` 子路由里。

### API 调用（`src/api/`，共 8 个文件）

| 文件 | 说明 |
|---|---|
| `auth.js` | 登录、注册 |
| `user.js` | 用户信息查询、充值、VIP 购买、密码修改 |
| `course.js` | 课程列表（含分类筛选）、课程详情、管理员 CRUD |
| `booking.js` | 创建/取消/支付预约 |
| `alipay.js` | 发起支付宝支付 |
| `banner.js` | Banner 展示与管理 |
| `report.js` | 数据驾驶舱统计 |
| `ai.js` | AI 聊天、会话管理 |

所有 HTTP 调用经过 `src/utils/request.js`（Axios 实例 + token 注入 + 错误拦截）。不要把 axios 调用零散写进 `.vue`。

`request.js` 响应拦截器中 `res.code === '200'` 是**字符串**比较（不是数字），定义新接口时 code 字段务必返回字符串 `"200"`。401 时清除 localStorage 并跳 `/login`。

### 常量（`src/constants/`，共 5 个文件）

| 文件 | 内容 |
|---|---|
| `role.js` | 用户角色常量（admin/user） |
| `booking.js` | 预约状态常量（PENDING/PAID/CANCELLED） |
| `vip.js` | VIP 类型常量（普通/月卡/年卡）及折扣信息 |
| `course.js` | 课程分类 `COURSE_CATEGORIES`（有氧训练、力量训练等 6 类）、`STATUS_OPTIONS`、`SORT_OPTIONS` |
| `theme.js` | 主题颜色常量 |

### 数据可视化

数据驾驶舱（`Home.vue`）使用 ECharts 6（`echarts@^6.0.0`）。注意 ECharts 6 与 5 的 API 有少量差异，参考已有图表实现。

### AI 集成（双通道）

项目中存在**两套独立的 AI 对话**，共存但互不干扰：

1. **Coze SDK 浮窗**（`index.html`）：第三方 AI 聊天机器人，通过 CDN 加载 Coze Web SDK，bot_id 和 PAT token 硬编码在 `<script>` 标签中。浮窗按钮出现在所有页面（包括登录页），是一个完全独立的外部 AI 通道。

2. **AiChat.vue**（`src/views/AiChat.vue`）：自建 AI 对话页面，路由挂在 Layout 子路由下。左侧会话列表 + 右侧流式聊天区，调用后端 `POST /ai/chat/stream` SSE 接口。后端 `AiServiceImpl` 对接 DeepSeek API（`deepseek-chat` 模型），支持多轮上下文（历史存 `ai_chat_session` / `ai_chat_message` 表），每次请求携带最近 20 条对话历史。`application.yml` 的 `deepseek.enabled` 为总开关，关闭时抛错提示"AI 功能未开启"。DeepSeek API Key 优先读环境变量 `DEEPSEEK_API_KEY`，未设置则回退到 yml 中的硬编码默认值。

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
- 新增业务异常优先继承 `BusinessException`，并附带 `ErrorCode` 枚举中对应的错误码。
- 新增枚举/状态时把"可读 API"（如 `canCancel()`、`isExpired()`）一起加上，避免下游写散开的 if/switch。
- 前后端默认端口是 8080 / 5173；CORS 在 `CorsConfig` 中配置，调整端口需同步两端。
- 课程分类字符串必须与 `src/constants/course.js` 中 `COURSE_CATEGORIES` 的 `value` 值保持一致，不要随意增删分类而不同步前端常量。
- 用户账号有 `status` 字段（0=禁用，1=正常），禁用用户登录时抛 `BIZ_USER_DISABLED`；管理员不能禁用自己（`BIZ_CANNOT_DISABLE_SELF`）。
