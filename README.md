# Gym Pro - 智能健身预约系统

一个前后端分离的智能健身房管理系统，支持课程预约、支付宝支付、VIP 会员、AI 智能教练和数据驾驶舱等功能。

## 项目结构

```
Gym/
├── gym-system/          # 后端：Spring Boot 3 + MyBatis-Plus
├── gym-vue/             # 前端：Vue 3 + Vite + Element Plus
├── docs/                # 架构决策记录 (ADR)
├── CONTEXT.md           # 领域语言定义
└── README.md
```

## 功能模块

| 模块 | 功能 |
|------|------|
| 用户认证 | 登录/注册、BCrypt 密码加密、JWT Token 鉴权、路由守卫 |
| 课程预约 | 课程浏览、高并发抢课（分布式锁）、库存扣减 |
| 订单支付 | 余额支付、支付宝沙箱支付、订单取消/退款 |
| VIP 会员 | 月卡/年卡购买与续费、VIP 折扣（策略模式）、到期自动降级 |
| AI 教练 | 上下文感知的智能对话、课程推荐、余额查询 |
| 数据驾驶舱 | 营收/用户/订单总览、会员分布饼图、营收趋势图 |
| 后台管理 | 用户管理（CRUD、余额调整）、课程管理（CRUD） |

## 技术栈

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 运行环境 |
| Spring Boot | 3.2.2 | 基础框架 |
| Spring Security | - | BCrypt 密码加密（仅用加密功能） |
| MyBatis-Plus | 3.5.5 | ORM 框架 |
| MySQL | 8.0 | 关系型数据库 |
| HikariCP | - | 数据库连接池 |
| Redisson | 3.27.0 | 分布式锁 |
| Hutool | 5.8.25 | 工具类库（JWT、雪花ID） |
| 支付宝 SDK | 4.38.61 | 沙箱支付 |
| Lombok | - | 代码简化 |

### 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4.19 | 前端框架（Composition API） |
| Vite | 5.1.4 | 构建工具 |
| Element Plus | 2.5.6 | UI 组件库 |
| Vue Router | 4.2.5 | 路由管理 |
| Axios | 1.6.7 | HTTP 客户端 |
| ECharts | 6.0.0 | 数据可视化 |

## 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.6+

### 1. 数据库初始化

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS gym_db DEFAULT CHARACTER SET utf8mb4;"

# 导入数据
mysql -u root -p gym_db < gym-vue/db_backups/gym_db4.sql

# 执行迁移脚本（密码字段扩展 + 状态注释修正）
mysql -u root -p gym_db < gym-vue/db_backups/migrate_v2.sql
```

### 2. 启动后端

```bash
cd gym-system

# 修改 application.yml 中的数据库连接信息（用户名/密码）
# 确保 MySQL 和 Redis 服务已启动

mvn spring-boot:run
```

后端启动后访问：`http://localhost:8080`

### 3. 启动前端

```bash
cd gym-vue

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端启动后访问：`http://localhost:5173`

### 4. 登录测试

> 注意：重构后密码使用 BCrypt 加密。已有用户首次登录时系统会自动将明文密码升级为 BCrypt 哈希。

| 账号 | 密码 | 角色 |
|------|------|------|
| admin | 123456 | 管理员 |
| cd | 123456 | 普通用户（月卡 VIP） |
| 陈东 | 123456 | 普通用户（年卡 VIP） |

## 架构设计

### 分层架构（后端）

```
Controller → Service → Mapper → Database
    ↓          ↓
   DTO       Entity
              ↓
           VO/Enum
```

- **Controller**：接收请求，调用 Service，返回 `Result<T>`
- **Service**：业务逻辑，事务管理，分布式锁
- **Mapper**：数据访问，MyBatis-Plus 通用 CRUD
- **DTO**：请求参数传输对象
- **VO**：响应视图对象（替代 Map 返回值）
- **Enum**：业务枚举（BookingStatus、VipType、UserRole）
- **Exception**：统一业务异常体系（BusinessException）

### 前端架构

```
views/ → api/ → utils/request.js → 后端 API
  ↓        ↓
components/ constants/
```

- **api/**：统一 API 调用层，所有接口集中管理
- **constants/**：业务常量（API 路径、状态码、VIP 类型）
- **utils/request.js**：Axios 封装（拦截器、Token、错误处理）

### 设计亮点

#### 高并发抢课

使用 **Redisson 分布式锁** 对每个课程加锁，防止超卖：

```
用户点击抢课 → 获取分布式锁 → 校验库存/重复/时间冲突
→ 原子扣减库存 → 计算折扣价格 → 生成订单 → 释放锁
```

#### 策略模式折扣

通过 `DiscountFactory` + `DiscountStrategy` 实现折扣计算：

- 普通会员：原价
- 月卡 VIP：9 折
- 年卡 VIP：8 折

#### 订单状态机

使用 `BookingStatus` 枚举统一管理，替代魔法数字：

```
PENDING(0) → PAID(1) → CANCELLED(2)
  ↓ canPay     ↓ canCancel
  ↓ canCancel
```

#### BCrypt 密码加密

- 新用户注册时直接使用 BCrypt 哈希存储
- 旧用户首次登录时自动升级明文密码为 BCrypt

#### 统一异常体系

- `BusinessException`：业务异常（余额不足、课程售罄等）
- `UnauthorizedException`：权限异常
- `GlobalExceptionHandler`：全局捕获，统一返回 `Result<Void>`

## 架构决策记录

| ADR | 标题 | 决策 |
|-----|------|------|
| 0001 | 分布式锁选型 | 使用 Redisson，按课程ID加锁 |
| 0002 | 折扣策略 | 策略模式 + 工厂模式 |
| 0003 | 密码存储 | BCrypt 哈希加密 |
| 0004 | 订单状态 | 使用枚举替代魔法数字 |
| 0005 | Mapper 返回值 | VO 替代 Map |

## 各模块详细文档

- [后端模块文档](./gym-system/README.md)
- [前端模块文档](./gym-vue/README.md)
- [领域语言定义](./CONTEXT.md)

买家账号：crrgvr8904@sandbox.com
支付密码：111111