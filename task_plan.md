# 积分商城系统 - 任务计划

## 项目目标
构建一套完整的积分商城系统，包含：
1. **App端**（移动端 H5/移动优先 Web 应用）：用户浏览商品、积分兑换、订单管理、个人中心
2. **后管平台**（Web 管理后台）：商品管理、订单管理、用户管理、积分规则、数据看板
3. **Spring Boot 后端服务**：RESTful API、JWT 鉴权、MySQL 持久化、Redis 缓存

## 技术栈决策
- **后端**：Spring Boot 3.x + Java 17 + MyBatis-Plus + MySQL 8 + Redis + JWT
- **App端**：Vue 3 + Vite + Vant 4（移动端 UI 库）+ Pinia + Axios
- **后管平台**：Vue 3 + Vite + Element Plus + Pinia + ECharts
- **构建**：Maven（后端）、npm（前端）
- **数据库**：MySQL 8，预置初始数据

## 阶段计划

### Phase 1: 市场调研 [completed]
- 调研主流积分商城产品（淘宝/京东积分、电信运营商积分、银行信用卡积分商城等）
- 总结核心功能模块、业务流程、积分规则
- 输出调研报告到 findings.md

### Phase 2: 系统设计 [completed]
- 设计数据库 ER 模型（用户、商品、分类、订单、积分账户、积分流水、地址等）
- 设计后端 API 规范（RESTful，统一响应体、错误码）
- 设计前端页面结构

### Phase 3: Spring Boot 后端开发 [completed]
- 项目骨架、配置（application.yml、pom.xml）
- 公共组件：统一响应、异常处理、JWT 工具、Redis 配置、MyBatis-Plus 配置
- 数据库 schema 与初始数据 SQL
- 业务模块：用户/认证、商品、分类、订单、积分、地址、上传
- 后管 API + App API 分层
- 验证：编译通过，启动成功，全流程 API 测试通过（注册/登录/签到/下单/发货/确认/积分流水）

### Phase 4: App 端开发 [completed]
- Vite + Vue 3 + Vant 项目骨架
- 页面：首页、分类、商品详情、兑换、订单、个人中心、登录、地址管理
- 接口对接

### Phase 5: 后管平台开发 [completed]
- Vite + Vue 3 + Element Plus 项目骨架
- 页面：登录、仪表盘、商品管理、分类管理、订单管理、用户管理、积分规则
- 接口对接

### Phase 6: 集成与验收 [completed]
- 后端启动验证、API 自测 ✅
- 前端构建、跨域配置 ✅（Vite proxy 代理）
- 整体联调 ✅（API 层 24/24 通过 + UI 层 15 页面渲染 + 2 登录流程通过）
- UI 截图验证 ✅（Playwright + chromium 127，14 张截图）
- 输出验收报告 ✅（见 ACCEPTANCE.md）

## 关键决策记录
- 单体 Spring Boot 后端，App 与后管共用同一后端，通过角色/权限区分
- 使用 MyBatis-Plus 减少样板代码
- 数据库使用 MySQL，无外部依赖时使用 H2 兼容模式作为备选
- 前端使用 Vue 3 + Vite，开发体验与现代性兼顾

## 错误记录
| 错误 | 尝试 | 解决方案 |
|------|------|----------|
