# 健身房管理系统 — 项目层级架构说明

## 项目整体布局

```
gym/                          # 根仓库
├── gym-system/               # 后端（Spring Boot 3 + Maven，独立 git 仓库）
├── gym-vue/                  # 前端（Vue 3 + Vite，独立 git 仓库）
├── docs/adr/                 # 架构决策记录（ADR 0001–0005）
└── CONTEXT.md                # 领域语言定义
```

两个子目录是**各自独立的 git 仓库**，根目录本身也是一个 git 仓库。

---

## 一、后端 `gym-system/` — Spring Boot 3 + Maven

### 1.1 Controller 层（`com.example.gym.controller`）

**职责：** 接收 HTTP 请求、参数校验、调用 Service、返回统一响应。

**约定：**
- 所有方法必须返回 `Result<T>`，不直接抛 `ResponseEntity` 或裸 Map
- 异常统一交给 `GlobalExceptionHandler` 转成 `Result.fail(...)`
- 使用 `@CurrentUserId Long userId` 参数注解获取当前用户 ID——**不要**从请求体或 URL 参数中取

| Controller | 前缀 | 功能说明 |
|---|---|---|
| `AuthController` | `/auth` | 登录（JWT 签发）、注册（BCrypt 哈希） |
| `UserController` | `/user` | 用户 CRUD、分页查询、充值与购买 VIP、个人资料/密码修改、启用/禁用 |
| `CourseController` | `/course` | 课程列表与详情、管理员 CRUD，支持分类/名称筛选 |
| `BookingController` | `/booking` | **高并发核心路径**——预约创建、取消、支付 |
| `AlipayController` | `/alipay` | 支付宝沙箱支付（课程/充值双模式）、同步回调、异步回调 |
| `BannerController` | `/banner` | 首页 Banner 展示（仅 status=1）与后台管理 |
| `ReportController` | `/report` | 数据驾驶舱统计：用户数、订单数、总收入、VIP 分布 |
| `AiController` | `/ai` | AI 客服对话（HTTP + SSE 流式）、会话列表/消息/删除 |

---

### 1.2 Service 层（`com.example.gym.service` + `service/impl/`）

**职责：** 核心业务逻辑实现，接口与实现分离。

**关键业务模块：**

#### 高并发抢课（`BookingServiceImpl`）
```
┌─────────────────────────────────────┐
│  Redisson 分布式锁                    │
│  锁键: gym:booking:lock:{courseId}  │
│  tryLock(3, 10, SECONDS)            │
│                                     │
│  临界区内:                            │
│  ① 库存校验 (stock > 0)              │
│  ② 重复/时间冲突校验                   │
│  ③ 原子扣减库存                       │
│  ④ 折扣计算 (DiscountFactory)        │
│  ⑤ 写订单                            │
│                                     │
│  finally 保证锁释放                   │
└─────────────────────────────────────┘
```
> **注意：** 修改这段代码时，锁粒度保持 courseId 级别，不要降级为全局锁；异常时 finally 仍要释放锁。

#### 折扣计算（策略模式）
```
DiscountFactory.get(vipType) → DiscountStrategy.apply(price)

VIP 类型:
  - 普通 (0):  原价
  - 月卡 (1):  折扣价
  - 年卡 (2):  折扣价
```
> **约定：** 新增 VIP 等级时只新增 `DiscountStrategy` 实现并在工厂注册，不要写 `if (vipType == ...)` 分支。

#### 密码管理
- 注册/修改密码用 **BCrypt** 哈希
- 登录时若发现旧数据为明文（历史数据），验证通过后**就地升级**为 BCrypt 再写回

#### AI 对话（`AiServiceImpl`）
- 对接 **DeepSeek API**（`deepseek-chat` 模型）
- 支持多轮上下文（每次携带最近 20 条历史）
- 历史记录存入 `ai_chat_session` / `ai_chat_message` 表
- `deepseek.enabled: false` 时全局关闭 AI 功能

---

### 1.3 Mapper 层（`com.example.gym.mapper`）

**职责：** 数据库访问，基于 MyBatis-Plus。

**约定：**
- 返回业务对象一律用 **VO**，不要用 `Map<String, Object>`（ADR-0005）
- 实体类使用 `@Version` 乐观锁字段（`version` 字段当前注释掉）

---

### 1.4 Entity / DTO / VO / Enum 支撑层

| 层级 | 包路径 | 作用 |
|---|---|---|
| **Entity** | `entity/` | 数据库表映射对象（`SysUser`、`GymCourse`、`GymBooking` 等） |
| **DTO** | `dto/` | 入参对象，接收前端提交的数据 |
| **VO** | `vo/` | 出参对象，返回给前端的结构化数据 |
| **Enum** | `enum/` | 状态/类型枚举，内含业务判断方法 |

**核心枚举：**

| 枚举 | 关键字段/方法 | 说明 |
|---|---|---|
| `BookingStatus` | `PENDING(0) → PAID(1) → CANCELLED(2)` | 订单状态机，提供 `canPay()`、`canCancel()` 方法 |
| `ErrorCode` | 200/400/401/403/404/4001–4302/4901/500 | 统一错误码，详见下表 |
| `VipType` | 0=普通, 1=月卡, 2=年卡 | VIP 等级 |

**错误码分段：**

| 范围 | 含义 |
|---|---|
| `200` | 成功 |
| `400` | 参数/业务异常（通用） |
| `401` | 未登录或 token 过期 |
| `403` | 无权访问 |
| `404` | 资源不存在 |
| `4001–4006` | 用户相关（不存在、密码错、用户名重复、原密码错、账号禁用、不能禁用自己） |
| `4101` | 余额不足 |
| `4201–4205` | 课程/预约相关（不存在、售罄、已过期、重复预约、时间冲突） |
| `4301–4302` | 订单相关（不存在、状态非法） |
| `4901` | 分布式锁超时（抢购人数过多） |
| `500` | 系统错误 |

---

### 1.5 Common / Config 横切层

| 组件 | 包路径 | 作用 |
|---|---|---|
| `Result<T>` | `common/` | 统一响应体 `{code, message, data}` |
| `GlobalExceptionHandler` | `common/` | 全局异常拦截 → 统一 `Result.fail(...)` |
| `BusinessException` | `common/` | 业务异常（附 ErrorCode），所有自定义异常优先继承它 |
| `UnauthorizedException` | `common/` | 未授权异常 |
| `JwtInterceptor` | `config/` | `HandlerInterceptor`，解析 `Authorization` 头，写入 `uid`/`role` 到 RequestAttribute |
| `@CurrentUserId` | `common/` | 参数注解，从 RequestAttribute 获取当前用户 ID |
| `CorsConfig` | `config/` | 跨域配置（前端 5173 ↔ 后端 8080） |
| `AliPayConfig` | `config/` | 支付宝沙箱配置（appId、公私钥、回调地址） |

**鉴权链路：**
```
前端请求头 Authorization: Bearer <JWT>
    ↓
JwtInterceptor.preHandle() → 解析 JWT → 写入 uid/role 到 RequestAttribute
    ↓
Controller 通过 @CurrentUserId Long userId 获取当前用户
```

> **注意：** Spring Security 依赖**只用作 BCrypt**，没有配置 `SecurityFilterChain`，不要假设有 Security 过滤链。

---

## 二、前端 `gym-vue/` — Vue 3 + Vite

### 2.1 视图层 `src/views/`（13 个页面）

| 文件 | 路由 | 功能说明 |
|---|---|---|
| `Login.vue` | `/login` | 登录页，白名单路由，未登录唯一可访问页面 |
| `Layout.vue` | `/`（壳） | 主布局（侧边栏导航 + 顶栏），所有受保护页面为其子路由 |
| `Home.vue` | `/home` | 数据驾驶舱，使用 **ECharts 6** 渲染图表 |
| `Course.vue` | `/course` | 课程列表（支持分类/状态/排序三种筛选维度） |
| `CourseDetail.vue` | `/course/:id` | 课程详情页，可发起预约 |
| `MyBooking.vue` | `/my-booking` | 用户预约记录，支持取消/支付 |
| `Wallet.vue` | `/wallet` | 钱包余额、充值入口、购买 VIP |
| `Profile.vue` | `/profile` | 个人资料/密码修改 |
| `AiChat.vue` | `/ai-chat` | 自建 AI 对话（左侧会话列表 + 右侧 SSE 流式聊天） |
| `PaySuccess.vue` | `/pay/success` | 支付宝支付同步回调落地页 |
| `AdminCourse.vue` | `/admin-course` | 课程后台管理（管理员专属） |
| `AdminUser.vue` | `/admin-user` | 用户后台管理（管理员专属） |
| `UserHome.vue` | — | 用户首页子组件（由 `Home.vue` 引用，非独立路由） |

---

### 2.2 路由与权限 `src/router/`

**技术：** Vue Router 4，`beforeEach` 守卫实现五重校验：

```
进入路由
  ↓
① /login? → 白名单，直接放行
  ↓ 否
② localStorage.token 和 localStorage.user 双重存在?
  ↓ 否 → 清除残留数据 → 跳 /login
  ↓ 是
③ JWT exp 字段过期?
  ↓ 是 → ElMessage.warning → 清除 → 跳 /login
  ↓ 否
④ /admin-* 路由? → 检查 user.role === 'admin'
  ↓ 否 → ElMessage.error → 跳首页
  ↓ 是 / 非 admin 路由
⑤ 放行 → 页面渲染
  ↓ (运行时)
后端 JwtInterceptor + Axios 401 拦截器兜底
```

**约定：**
- 新增管理员页面必须以 `admin-` 前缀命名，否则绕过权限
- 新增受保护页面挂在 `Layout` 子路由下

---

### 2.3 API 层 `src/api/`（8 个文件）

**职责：** 封装所有 HTTP 请求，不把 axios 零散写进 `.vue`。

| 文件 | 功能 |
|---|---|
| `auth.js` | 登录、注册 |
| `user.js` | 用户信息查询、充值、VIP 购买、密码修改 |
| `course.js` | 课程列表（含分类筛选）、课程详情、管理员课程 CRUD |
| `booking.js` | 创建/取消/支付预约 |
| `alipay.js` | 发起支付宝支付 |
| `banner.js` | Banner 展示与管理 |
| `report.js` | 数据驾驶舱统计数据 |
| `ai.js` | AI 聊天、会话管理 |

**统一 Axios 实例（`src/utils/request.js`）：**
```
请求拦截器:  注入 Authorization token
响应拦截器:  res.code === '200' (字符串!) → 返回 data
            code === '401' → 清除 localStorage → 跳 /login
          其他 → ElMessage.error → Promise.reject
```
> **重要：** 响应拦截器中 code 比较是字符串 `"200"`，不是数字 `200`。定义新接口时 code 字段务必返回字符串。

---

### 2.4 常量层 `src/constants/`（5 个文件）

| 文件 | 内容 |
|---|---|
| `role.js` | 用户角色常量（`admin` / `user`） |
| `booking.js` | 预约状态常量（`PENDING` / `PAID` / `CANCELLED`） |
| `vip.js` | VIP 类型（普通/月卡/年卡）及对应折扣信息 |
| `course.js` | `COURSE_CATEGORIES`（6 类课程分类）、`STATUS_OPTIONS`、`SORT_OPTIONS` |
| `theme.js` | 主题颜色常量 |

> **约定：** 后端课程分类字符串必须与 `COURSE_CATEGORIES` 的 `value` 值保持一致，增删分类需前后端同步。

---

### 2.5 状态管理

**当前无 Pinia/Vuex。** 用户状态通过 `localStorage` 直接读写：
- 各组件自行 `JSON.parse(localStorage.getItem('user'))`
- 路由守卫也直接读 `localStorage.user`
- Token 存 `localStorage.token`

---

### 2.6 AI 集成（双通道共存）

| 通道 | 实现方式 | 说明 |
|---|---|---|
| **Coze SDK 浮窗** | `index.html` 中 CDN 加载 + 硬编码 bot_id/PAT | 第三方 AI 机器人，所有页面（含登录页）出现浮窗按钮，完全独立 |
| **AiChat.vue** | 自建页面 + SSE 流式 + 后端 DeepSeek API | 路由保护，会话持久化，多轮上下文 |

---

## 三、数据库 MySQL `gym_db1`

### 3.1 主要数据表

| 表名 | 对应实体 | 关键字段 |
|---|---|---|
| `sys_user` | `SysUser` | id, username, password(BCrypt), role, balance, vipType, vipExpireTime, status |
| `gym_course` | `GymCourse` | id, name, coach, category, startTime, capacity, stock, price, version |
| `gym_booking` | `GymBooking` | id, userId, courseId, status(PENDING/PAID/CANCELLED), actualPrice, createTime |
| `ai_chat_session` | — | AI 对话会话记录 |
| `ai_chat_message` | — | AI 对话消息记录（多轮上下文） |

### 3.2 数据库初始化

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS gym_db1 DEFAULT CHARACTER SET utf8mb4;"
mysql -u root -p gym_db1 < gym-vue/db_backups/gym_db4.sql
```

---

## 四、全栈数据流总览

```
┌──────────────────────────────────────────────────────────────────────┐
│  浏览器                                                                │
│                                                                      │
│  Vue Router (beforeEach 五重守卫)                                      │
│       ↓                                                              │
│  Views (.vue)  ←── 直接读 localStorage (无 Pinia/Vuex)                 │
│       ↓ 调用                                                          │
│  src/api/*.js  ──→  src/utils/request.js (Axios 实例)                 │
│       │                  │                                           │
│       │                  ├─ 请求头注入 Authorization: Bearer <JWT>     │
│       │                  └─ 响应 code==='401' → 清除 localStorage → /login│
└───────┼──────────────────┼───────────────────────────────────────────┘
        │ HTTP             │
        ▼                  ▼
┌───────────────────────────────────────────────────────────────────────┐
│  Spring Boot (gym-system)                                             │
│                                                                       │
│  CorsConfig 允许跨域                                                    │
│       ↓                                                               │
│  JwtInterceptor 解析 JWT → uid/role 写入 RequestAttribute               │
│       ↓                                                               │
│  Controller (@CurrentUserId 获取用户) → 返回 Result<T>                  │
│       ↓ 调用                                                           │
│  Service (接口 + impl 分离)                                             │
│       │                                                               │
│       ├─ 高并发: Redisson 分布式锁 (gym:booking:lock:{courseId})        │
│       ├─ 折扣:   DiscountFactory → DiscountStrategy.apply()            │
│       ├─ 密码:   BCrypt + 旧明文就地升级                                 │
│       └─ AI:    DeepSeek API (多轮上下文, 最近20条)                      │
│       ↓                                                               │
│  Mapper (MyBatis-Plus) → MySQL (gym_db1)                               │
│       ↑                                                               │
│  GlobalExceptionHandler ← BusinessException/UnauthorizedException      │
│       统一返回 Result.fail(code, message)                               │
└───────────────────────────────────────────────────────────────────────┘
```

---

## 五、技术栈速览

| 层面 | 技术 |
|---|---|
| 后端框架 | Spring Boot 3 |
| 语言 | Java 17 |
| 构建 | Maven |
| ORM | MyBatis-Plus |
| 数据库 | MySQL 8.0 |
| 缓存/锁 | Redis 6.0+（Redisson） |
| JWT | Hutool JWT（`cn.hutool.jwt`） |
| 密码 | BCrypt（Spring Security Crypto） |
| 支付 | 支付宝沙箱 |
| AI | DeepSeek API (`deepseek-chat`) |
| 前端框架 | Vue 3 (Composition API) |
| 构建 | Vite |
| 路由 | Vue Router 4 |
| UI 库 | Element Plus |
| 图表 | ECharts 6 |
| HTTP | Axios |
| 第三方 AI | Coze Web SDK (CDN) |

---

## 六、测试账号

| 账号 | 密码 | 角色 |
|---|---|---|
| `admin` | `123456` | 管理员 |
| `cd` | `123456` | 普通用户（月卡 VIP） |
| `陈东` | `123456` | 普通用户（年卡 VIP） |

---

## 七、运行环境依赖

| 组件 | 版本 | 说明 |
|---|---|---|
| JDK | 17+ | 后端编译与运行 |
| Node | 18+ | 前端开发与构建 |
| MySQL | 8.0 | 默认 `localhost:3306/gym_db1`，账号 `root`/`123456` |
| Redis | 6.0+ | 默认 `localhost:6379`，**未启动则抢课接口直接失败** |