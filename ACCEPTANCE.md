# 积分商城系统 - 验收报告

> 交付日期：2026-07-23
> 验收结论：**通过** ✅

---

## 一、项目概述

本项目为一套完整的积分商城系统，包含三大部分：

| 模块 | 技术栈 | 代码规模 |
|------|--------|----------|
| 后端服务 (server/) | Spring Boot 3.2.5 + Java 17 + MyBatis-Plus 3.5.5 + MySQL/MariaDB + Redis + JWT | 63 个 Java 文件，2871 行 |
| App 端 (app/) | Vue 3 + Vite 8 + Vant 4 + Pinia + Axios | 23 个 Vue/JS 文件，3160 行 |
| 后管平台 (admin/) | Vue 3 + Vite + Element Plus + ECharts + Pinia + Axios | 25 个 Vue/JS 文件，2843 行 |
| 数据库 | MariaDB 10.11（MySQL 兼容） | 9 张表 + 初始数据 |

合计约 **8874 行代码**，覆盖完整业务闭环。

---

## 二、市场调研结论

调研了主流积分商城产品（淘宝/京东积分、电信运营商积分、银行信用卡积分商城等），提炼出以下核心规则并落地实现：

### 积分规则
- **积分获取**：消费 1 元 = 1 积分；签到 5 积分/天；注册赠送 100 积分
- **积分使用**：商品兑换（纯积分 / 积分+现金）；100 积分 = 1 元抵扣
- **会员等级**：4 级体系
  - 普通会员：0+ 积分，折扣 1.0
  - 银卡会员：1000+ 积分，折扣 0.95
  - 金卡会员：5000+ 积分，折扣 0.90
  - 钻石会员：10000+ 积分，折扣 0.85

### 核心业务模块
- 用户认证（注册/登录/JWT 双 token）
- 商品浏览（首页/分类/详情/搜索）
- 积分兑换（下单/库存扣减/积分扣减/会员折扣）
- 订单管理（待发货/已发货/已完成/已取消 全流程）
- 签到打卡（每日签到+连续签到）
- 个人中心（积分/等级/地址/资料/密码）
- 后管数据看板（ECharts 可视化）
- 商品/分类/订单/用户/轮播图/积分流水管理

---

## 三、验收结果

### 1. API 接口验收（24/24 通过 ✅）

通过 curl 对全部 24 个核心接口进行端到端测试，结果 **24/24 全部通过**：

**公开接口（5/5）**：App 首页、轮播图、分类、商品列表、商品详情

**App 鉴权流程（6/6）**：登录获取 Token、个人信息、积分信息、积分流水、地址列表、订单列表

**Admin 鉴权流程（11/11）**：登录获取 Token、仪表盘概览、订单状态分布、积分趋势、热销商品、用户列表、分类列表、商品列表、订单列表、轮播图、积分流水

**安全验证（2/2）**：未登录访问受保护接口返回 401 JSON、无 Token 访问 Admin 返回 401 JSON

### 2. 完整业务流程验收（端到端 ✅）

通过 curl 模拟完整用户旅程，验证业务一致性：

1. **注册** → 赠送 100 积分 ✅
2. **登录** → 获取 JWT Token ✅
3. **签到** → +5 积分，记录积分流水 ✅
4. **管理员调整积分** → 积分变更+流水记录 ✅
5. **下单** → 积分扣减 + 库存乐观锁扣减 + 会员折扣（钻石 85%）✅
6. **管理员发货** → 订单状态变更为已发货 ✅
7. **用户确认收货** → 订单完成 + 返还积分 + 流水记录 ✅
8. **积分流水查询** → 完整记录所有积分变动 ✅

**关键机制验证**：
- 乐观锁防超卖 ✅
- 事务一致性（订单+积分+库存同事务）✅
- JWT 双 Token（APP/ADMIN 独立）✅
- Spring Security 角色控制 ✅

### 3. UI 界面验收（Playwright 截图 ✅）

使用 Playwright + chromium 127 对前端进行 UI 自动化验证，生成 **14 张截图**：

**App 端（8 项全通过）**：
| 页面 | 结果 |
|------|------|
| 首页 /home | ✅ 轮播图/图片渲染正常 |
| 商品列表 /products | ✅ 渲染正常 |
| 商品详情 /product/1 | ✅ 含商品信息 |
| 登录页 /login | ✅ 含登录按钮 |
| 登录流程 | ✅ 登录后跳转 /home |
| 个人中心 /profile | ✅ 显示积分+会员等级 |
| 签到页 /sign | ✅ 渲染正常 |
| 订单列表 /orders | ✅ 渲染正常（修复 addressSnapshot 解析 bug 后） |

**后管平台（7 项全通过）**：
| 页面 | 结果 |
|------|------|
| 登录页 /login | ✅ 渲染正常 |
| 登录流程 | ✅ 登录后跳转 /dashboard |
| 仪表盘 /dashboard | ✅ 含统计数据 + ECharts 图表 |
| 用户管理 /users | ✅ 含用户数据 |
| 商品管理 /products | ✅ 含商品数据 |
| 订单管理 /orders | ✅ 含订单数据 |
| 分类管理 /categories | ✅ 含分类数据 |

---

## 四、修复的问题

验收过程中发现并修复 1 个 bug：

| 问题 | 位置 | 修复 |
|------|------|------|
| 订单列表 addressSnapshot 解析失败，控制台报 JSON 解析错误 | app/src/views/Orders.vue `addressText()` | 后端快照格式为 `name\|phone\|address` 管道分隔，前端原按 JSON 解析。改为兼容两种格式（JSON / 管道分隔）|

---

## 五、运行环境与启动方式

### 依赖服务
- **MariaDB**：运行于 3306，数据库 `score_mall`，root 密码 `root`
- **Redis**：运行于 6379

### 启动命令
```bash
# 后端（端口 8080，context-path=/api）
cd /workspace/server && mvn -s /workspace/maven-settings.xml spring-boot:run

# App 端（端口 5173）
cd /workspace/app && npm run dev

# 后管平台（端口 5174）
cd /workspace/admin && npm run dev
```

### 测试账号
| 角色 | 账号 | 密码 |
|------|------|------|
| App 用户 | 13800000001 | 123456 |
| 管理员 | admin | admin123 |

---

## 六、交付清单

```
/workspace/
├── server/                          # Spring Boot 后端
│   ├── pom.xml                      # Maven 依赖（Spring Boot 3.2.5, MyBatis-Plus, JWT, Hutool, Lombok）
│   └── src/main/
│       ├── java/com/score/mall/     # 63 个 Java 文件
│       │   ├── ScoreMallApplication.java
│       │   ├── common/              # 统一响应、异常处理、分页
│       │   ├── config/              # Security/MyBatisPlus/Redis/WebMvc 配置
│       │   ├── security/            # JWT 工具、登录上下文、认证过滤器
│       │   ├── entity/              # 9 个实体
│       │   ├── mapper/              # 9 个 Mapper
│       │   ├── dto/                 # 6 个 DTO
│       │   ├── service/             # 11 个 Service
│       │   └── controller/
│       │       ├── app/             # 6 个 App Controller
│       │       └── admin/           # 9 个 Admin Controller
│       └── resources/
│           ├── application.yml      # 应用配置
│           └── sql/schema.sql       # 9 张表 DDL + 初始数据
├── app/                             # App 端（Vue 3 + Vant）
│   ├── vite.config.js               # 端口 5173，/api 代理
│   └── src/
│       ├── api/                     # 接口封装
│       ├── store/                   # Pinia 状态管理
│       ├── router/                  # 15 路由 + 守卫
│       ├── components/              # TabBar 组件
│       └── views/                   # 15 个页面
├── admin/                           # 后管平台（Vue 3 + Element Plus）
│   ├── vite.config.js               # 端口 5174，/api 代理
│   └── src/
│       ├── api/                     # 9 个 API 模块
│       ├── stores/                  # Pinia 状态管理
│       ├── router/                  # 路由 + 守卫
│       ├── layout/                  # 后台布局
│       ├── components/              # 图片上传组件
│       └── views/                   # 10 个页面
├── maven-settings.xml               # Maven 代理+镜像配置
├── task_plan.md                     # 任务计划（6 阶段全部完成）
├── findings.md                      # 市场调研报告
├── progress.md                      # 进度日志
├── verify_ui.py                     # Playwright UI 验证脚本
└── verify-*.png                     # 14 张 UI 验收截图
```

---

## 七、验收结论

| 验收项 | 结果 |
|--------|------|
| 后端编译启动 | ✅ 通过 |
| API 接口（24 个） | ✅ 24/24 通过 |
| 完整业务流程（8 步） | ✅ 全通过 |
| 事务/乐观锁/双 Token | ✅ 验证通过 |
| App 端 UI（8 页面） | ✅ 全通过 |
| 后管平台 UI（7 页面） | ✅ 全通过 |
| 安全（401 鉴权） | ✅ 通过 |

**综合结论：系统功能完整、业务闭环、前后端联调正常、UI 渲染正常，验收通过，可交付。**
