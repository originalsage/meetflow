# MeetFlow 会议室预约系统

## 📖 项目介绍

MeetFlow 是一个基于 Spring Boot 和 Vue 3 开发的现代化会议室预约管理系统。系统提供了完整的会议室管理、预约申请、审批流程、使用状态跟踪等功能，支持多角色权限管理（普通用户、管理员、超级管理员），帮助企业或组织高效管理会议室资源。

### 核心功能

- **用户认证与授权**：基于 JWT 的认证机制，支持用户注册、登录、权限管理
- **会议室管理**：会议室信息的增删改查，支持照片上传、状态管理
- **预约管理**：创建预约、审批流程、取消预约、完成确认
- **时间冲突检测**：自动检测会议室时间冲突和用户时间段重叠
- **数据可视化**：每日/每周预约情况展示，当前使用状态跟踪
- **权限分级**：普通用户、管理员、超级管理员三级权限体系

---

## 🛠️ 技术栈

### 后端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| **Spring Boot** | 4.0.0 | Java 后端框架 |
| **MyBatis** | 4.0.0 | ORM 框架 |
| **MySQL** | 5.7+ / 8.0+ | 关系型数据库 |
| **JWT** | 0.12.3 | 身份认证 |
| **Spring Security Crypto** | - | 密码加密 |
| **Spring Boot Validation** | - | 参数校验 |
| **阿里云 OSS** | 3.17.4 | 对象存储服务 |
| **Hutool** | 5.8.23 | Java 工具类库 |
| **Lombok** | 1.18.34 | 简化 Java 代码 |
| **JDK** | 17+ | Java 开发环境 |

### 前端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| **Vue** | 3.5.25 | 渐进式 JavaScript 框架 |
| **Vue Router** | 4.4.5 | 路由管理 |
| **Pinia** | 2.2.6 | 状态管理 |
| **Element Plus** | 2.8.8 | UI 组件库 |
| **Axios** | 1.7.7 | HTTP 客户端 |
| **Day.js** | 1.11.13 | 日期处理库 |
| **Vite** | 7.2.4 | 构建工具 |
| **Node.js** | ^20.19.0 或 >=22.12.0 | 运行环境 |

---

## 📁 项目结构

```
meetflow/
├── backend/                    # 后端项目
│   └── meetflow/               # Spring Boot 项目
│       ├── src/
│       │   ├── main/
│       │   │   ├── java/com/lls/
│       │   │   │   ├── common/          # 通用工具类
│       │   │   │   │   ├── JwtUtil.java      # JWT 工具类
│       │   │   │   │   ├── Result.java       # 统一响应封装
│       │   │   │   │   ├── ResultCode.java  # 错误码枚举
│       │   │   │   │   └── PageResult.java   # 分页结果封装
│       │   │   │   ├── config/           # 配置类
│       │   │   │   │   ├── CorsConfig.java      # 跨域配置
│       │   │   │   │   └── MyBatisConfig.java   # MyBatis 配置
│       │   │   │   ├── controller/      # 控制器层
│       │   │   │   │   ├── AuthController.java         # 认证控制器
│       │   │   │   │   ├── UserController.java         # 用户管理控制器
│       │   │   │   │   ├── MeetingRoomController.java  # 会议室控制器
│       │   │   │   │   ├── ReservationController.java  # 预约控制器
│       │   │   │   │   └── GlobalExceptionHandler.java # 全局异常处理
│       │   │   │   ├── service/         # 服务层接口
│       │   │   │   │   └── impl/        # 服务层实现
│       │   │   │   ├── mapper/          # 数据访问层
│       │   │   │   ├── entity/          # 实体类
│       │   │   │   ├── dto/             # 数据传输对象
│       │   │   │   └── vo/              # 视图对象
│       │   │   └── resources/
│       │   │       ├── application.yml  # 配置文件
│       │   │       └── mapper/          # MyBatis XML 映射文件
│       │   └── test/                    # 测试代码
│       └── pom.xml                      # Maven 配置
│
├── frontend/                   # 前端项目
│   └── meetflow/              # Vue 3 项目
│       ├── src/
│       │   ├── api/           # API 接口定义
│       │   ├── components/    # 公共组件
│       │   ├── layout/        # 布局组件
│       │   ├── router/        # 路由配置
│       │   ├── stores/        # Pinia 状态管理
│       │   ├── utils/          # 工具函数
│       │   └── views/          # 页面组件
│       │       ├── admin/     # 管理员页面
│       │       └── ...        # 用户页面
│       ├── package.json       # npm 配置
│       └── vite.config.js     # Vite 配置
│
└── README.md                  # 项目说明文档
```

---

## 🏗️ 模块实现方式

### 1. 认证模块 (Auth)

**实现方式：**
- 使用 JWT (JSON Web Token) 进行无状态身份认证
- Token 存储在客户端（LocalStorage），有效期为 24 小时
- 密码使用 Spring Security BCrypt 加密存储
- 请求拦截器自动在请求头中添加 `Authorization: Bearer {token}`

**核心类：**
- `JwtUtil.java`：JWT Token 生成、解析、验证
- `AuthController.java`：注册、登录、获取用户信息、更新用户信息
- `AuthService.java`：业务逻辑处理

**API 接口：**
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录
- `GET /api/auth/info` - 获取当前用户信息
- `PUT /api/auth/update` - 更新用户信息

### 2. 用户管理模块 (User)

**实现方式：**
- 三级权限体系：普通用户(0)、管理员(1)、超级管理员(2)
- 超级管理员可以管理所有用户，包括权限提升/降级
- 使用 MyBatis 进行数据库操作

**核心功能：**
- 用户列表查询（仅超级管理员）
- 用户删除（仅超级管理员）
- 权限提升/降级（仅超级管理员）

**API 接口：**
- `GET /api/users` - 获取所有用户列表
- `DELETE /api/users/{id}` - 删除用户
- `PUT /api/users/promote` - 提升用户权限
- `PUT /api/users/demote` - 降级用户权限

### 3. 会议室管理模块 (Meeting Room)

**实现方式：**
- 会议室信息 CRUD 操作
- 支持会议室照片上传到阿里云 OSS
- 会议室状态管理（可预约/不可预约）
- 根据条件筛选可用会议室（日期、时间段、容量）

**核心功能：**
- 会议室列表查询（公开）
- 会议室详情查询（公开）
- 可用会议室筛选（公开）
- 会议室增删改查（管理员）
- 会议室照片上传（管理员）
- 会议室状态设置（管理员）

**API 接口：**
- `GET /api/meeting-rooms` - 获取所有会议室
- `GET /api/meeting-rooms/{id}` - 获取会议室详情
- `GET /api/meeting-rooms/available` - 筛选可用会议室
- `POST /api/meeting-rooms` - 添加会议室（管理员）
- `PUT /api/meeting-rooms/{id}` - 修改会议室（管理员）
- `DELETE /api/meeting-rooms/{id}` - 删除会议室（管理员）
- `PUT /api/meeting-rooms/{id}/status` - 设置会议室状态（管理员）
- `POST /api/meeting-rooms/upload` - 上传会议室照片（管理员）

### 4. 预约管理模块 (Reservation)

**实现方式：**
- 预约状态流转：待审批(0) → 已通过(1) / 已驳回(2) → 已取消(3) / 已完成(4)
- 时间冲突检测：检查会议室时间冲突和用户时间段重叠
- 分页查询支持
- 预约审批流程（管理员）

**核心功能：**
- 创建预约（自动检测冲突）
- 查询我的预约记录（支持状态筛选、分页）
- 取消预约（仅待审批和已通过状态可取消）
- 审批预约（管理员：通过/驳回）
- 完成预约（确认使用）
- 预约情况查询（每日/每周，管理员）

**业务规则：**
- 同一用户在同一时间段不能预约多个会议室
- 创建预约时自动检测会议室时间冲突
- 审批时再次验证会议室状态和容量
- 只能取消待审批(0)或已通过(1)的预约

**API 接口：**
- `POST /api/reservations` - 创建预约
- `GET /api/reservations/my` - 查询我的预约记录
- `GET /api/reservations/my/page` - 分页查询我的预约记录
- `GET /api/reservations/{id}` - 获取预约详情
- `PUT /api/reservations/{id}/cancel` - 取消预约
- `GET /api/reservations/all` - 查询所有预约记录（管理员）
- `PUT /api/reservations/{id}/approve` - 审批预约（管理员）
- `GET /api/reservations/weekly` - 获取一周预约情况（管理员）
- `GET /api/reservations/daily` - 获取当天占用情况（管理员）
- `GET /api/reservations/current-usage` - 获取当前使用状态
- `PUT /api/reservations/{id}/complete` - 完成预约

### 5. 文件上传模块 (OSS)

**实现方式：**
- 集成阿里云 OSS 对象存储服务
- 支持图片文件上传（会议室照片）
- 文件大小限制：10MB
- 配置通过环境变量读取

**核心类：**
- `OssService.java`：OSS 服务接口
- `OssServiceImpl.java`：OSS 服务实现

**配置项：**
- `OSS_ENDPOINT`：OSS 端点地址
- `OSS_ACCESS_KEY_ID`：AccessKey ID
- `OSS_ACCESS_KEY_SECRET`：AccessKey Secret
- `OSS_BUCKET_NAME`：存储桶名称
- `OSS_URL_PREFIX`：访问地址前缀

---

## 🚀 快速开始

### 环境要求

#### 后端环境
- **JDK**: 17 或更高版本
- **Maven**: 3.6+ 
- **MySQL**: 5.7+ 或 8.0+

#### 前端环境
- **Node.js**: ^20.19.0 或 >=22.12.0
- **npm**: 随 Node.js 安装

---

## 📦 安装与运行

### 1. 数据库配置

#### 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS meetflow 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_unicode_ci;
```

#### 建表语句

```sql
-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（加密后）',
    `name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
    `role` INT NOT NULL DEFAULT 0 COMMENT '角色：0-普通用户，1-管理员，2-超级管理员',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 会议室表
CREATE TABLE IF NOT EXISTS `meeting_room` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '会议室ID',
    `name` VARCHAR(100) NOT NULL COMMENT '会议室名称',
    `room_number` VARCHAR(50) NOT NULL COMMENT '房号',
    `capacity` INT NOT NULL COMMENT '容纳人数',
    `area` DECIMAL(10,2) DEFAULT NULL COMMENT '面积（平方米）',
    `purpose` VARCHAR(500) DEFAULT NULL COMMENT '用途描述',
    `photo_url` VARCHAR(500) DEFAULT NULL COMMENT '照片URL（阿里云OSS）',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态：0-不可预约，1-可预约',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_room_number` (`room_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会议室表';

-- 预约记录表
CREATE TABLE IF NOT EXISTS `reservation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '预约ID',
    `user_id` BIGINT NOT NULL COMMENT '预约用户ID',
    `meeting_room_id` BIGINT NOT NULL COMMENT '会议室ID',
    `meeting_title` VARCHAR(200) NOT NULL COMMENT '会议主题',
    `reservation_date` DATE NOT NULL COMMENT '预约日期',
    `start_time` INT NOT NULL COMMENT '开始时间（小时，0-23）',
    `end_time` INT NOT NULL COMMENT '结束时间（小时，0-23）',
    `attendee_count` INT NOT NULL COMMENT '参会人数',
    `status` INT NOT NULL DEFAULT 0 COMMENT '状态：0-待审批，1-已通过，2-已驳回，3-已取消，4-已完成',
    `reject_reason` VARCHAR(500) DEFAULT NULL COMMENT '驳回理由',
    `approve_time` DATETIME DEFAULT NULL COMMENT '审批时间',
    `approver_id` BIGINT DEFAULT NULL COMMENT '审批人ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_meeting_room_id` (`meeting_room_id`),
    KEY `idx_reservation_date` (`reservation_date`),
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_reservation_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_reservation_meeting_room` FOREIGN KEY (`meeting_room_id`) REFERENCES `meeting_room` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预约记录表';
```

#### 配置数据库连接

编辑 `backend/meetflow/src/main/resources/application.yml` 文件：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/meetflow?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root        # 修改为你的MySQL用户名
    password: 123456      # 修改为你的MySQL密码
```

#### 初始化超级管理员（可选）

如果需要创建超级管理员账号，可以先注册一个用户，然后执行以下 SQL：

```sql
-- 将指定用户设置为超级管理员（role=2）
UPDATE user SET role = 2 WHERE username = 'your_username';
```

或者使用项目中的 `backend/meetflow/update_root_user.sql` 文件：

```sql
-- 将root用户的role设置为2（超级管理员）
UPDATE user SET role = 2 WHERE username = 'root';
```

---

### 2. 后端启动

#### 进入后端目录

```bash
cd backend/meetflow
```

#### 安装依赖（首次运行）

```bash
mvn clean install
```

或者使用 IDE（如 IntelliJ IDEA、Eclipse）自动导入 Maven 项目。

#### 配置环境变量（可选）

如果使用阿里云 OSS 存储会议室照片，需要配置以下环境变量：

- `OSS_ENDPOINT`: OSS 端点地址（默认：https://oss-cn-beijing.aliyuncs.com）
- `OSS_ACCESS_KEY_ID`: OSS AccessKey ID
- `OSS_ACCESS_KEY_SECRET`: OSS AccessKey Secret
- `OSS_BUCKET_NAME`: OSS 存储桶名称（默认：meetflow-room-photos）
- `OSS_URL_PREFIX`: OSS 访问地址前缀

**注意**：如果不配置 OSS，文件上传功能可能无法正常使用。

#### 启动 Spring Boot 应用

**方式一：使用 Maven 命令启动**

```bash
mvn spring-boot:run
```

**方式二：使用 IDE 启动**

1. 打开 `backend/meetflow/src/main/java/com/lls/MeetflowApplication.java`
2. 运行 `main` 方法

**方式三：打包后运行**

```bash
# 打包
mvn clean package

# 运行 jar 包
java -jar target/meetflow-0.0.1-SNAPSHOT.jar
```

#### 验证启动

启动成功后，访问以下地址验证：

- API 基础地址：`http://localhost:8080/api`
- 健康检查：访问 `http://localhost:8080/api` 查看接口响应

---

### 3. 前端启动

#### 进入前端目录

```bash
cd frontend/meetflow
```

#### 安装依赖（首次运行）

```bash
npm install
```

如果安装速度较慢，可以使用国内镜像：

```bash
# 使用淘宝镜像
npm install --registry=https://registry.npmmirror.com
```

#### 配置 API 地址（如需要）

检查 `frontend/meetflow/src/utils/request.js` 文件，确认 API 基础地址是否正确：

```javascript
// 默认应该是 http://localhost:8080/api
baseURL: 'http://localhost:8080/api'
```

#### 启动开发服务器

```bash
npm run dev
```

启动成功后，控制台会显示访问地址，通常是：`http://localhost:5173`

#### 构建生产版本（可选）

如果需要构建生产版本：

```bash
npm run build
```

构建完成后，静态文件会生成在 `frontend/meetflow/dist` 目录下。

#### 预览生产版本（可选）

```bash
npm run preview
```

---

## ❓ 常见问题

### 1. 数据库连接失败

**问题**：启动后端时提示数据库连接失败

**解决方案**：
- 检查 MySQL 服务是否启动
- 确认 `application.yml` 中的数据库连接信息是否正确
- 确认数据库 `meetflow` 是否已创建
- 检查 MySQL 用户权限

### 2. 端口被占用

**问题**：8080 端口已被占用

**解决方案**：
- 修改 `application.yml` 中的 `server.port` 配置
- 或者关闭占用 8080 端口的其他程序

### 3. Maven 依赖下载失败

**问题**：`mvn install` 时依赖下载失败

**解决方案**：
- 检查网络连接
- 配置 Maven 国内镜像（如阿里云镜像）
- 清理 Maven 本地仓库后重新下载

### 4. Node.js 版本不匹配

**问题**：前端启动时提示 Node.js 版本不符合要求

**解决方案**：
- 安装符合要求的 Node.js 版本（^20.19.0 或 >=22.12.0）
- 使用 nvm（Node Version Manager）管理多个 Node.js 版本

### 5. 前端无法连接后端

**问题**：前端页面可以打开，但无法调用后端 API

**解决方案**：
- 确认后端服务已启动（访问 `http://localhost:8080/api` 测试）
- 检查前端 `request.js` 中的 API 地址配置
- 检查浏览器控制台的错误信息
- 确认后端 CORS 配置正确

### 6. OSS 文件上传失败

**问题**：上传会议室照片时失败

**解决方案**：
- 检查 OSS 环境变量是否配置
- 确认 OSS AccessKey 和 Secret 是否正确
- 确认 OSS Bucket 是否存在且有写入权限
- 如果不使用 OSS，可以暂时跳过照片上传功能

---

## 📚 API 文档

详细的 API 接口文档请参考：`backend/meetflow/API接口文档.md`

### 接口分类

1. **认证模块** (`/api/auth`)
   - 用户注册、登录、获取用户信息、更新用户信息

2. **用户管理模块** (`/api/users`)
   - 用户列表查询、删除、权限管理（仅超级管理员）

3. **会议室模块** (`/api/meeting-rooms`)
   - 会议室 CRUD、可用会议室筛选、照片上传（管理员）

4. **预约模块** (`/api/reservations`)
   - 预约创建、查询、取消、审批、完成、统计查询

---

## 🔐 权限说明

### 用户角色

- **普通用户 (role=0)**：
  - 查看会议室列表和详情
  - 创建预约
  - 查看和管理自己的预约
  - 取消自己的预约（仅待审批和已通过状态）

- **管理员 (role=1)**：
  - 拥有普通用户的所有权限
  - 会议室管理（增删改查）
  - 预约审批（通过/驳回）
  - 查看所有预约记录
  - 查看每日/每周预约情况

- **超级管理员 (role=2)**：
  - 拥有管理员的所有权限
  - 用户管理（查看、删除、权限提升/降级）

---

## 📝 预约状态说明

| 状态码 | 状态名称 | 说明 |
|--------|---------|------|
| 0 | 待审批 | 用户提交预约，等待管理员审批 |
| 1 | 已通过 | 管理员审批通过，可以正常使用 |
| 2 | 已驳回 | 管理员驳回预约，需要查看驳回理由 |
| 3 | 已取消 | 用户主动取消预约 |
| 4 | 已完成 | 用户确认使用完成 |

---

## 🎯 核心业务规则

1. **时间冲突检测**：
   - 创建预约时自动检测会议室时间冲突
   - 同一用户在同一时间段不能预约多个会议室
   - 审批时再次验证时间冲突

2. **预约取消规则**：
   - 只能取消状态为"待审批"(0)或"已通过"(1)的预约
   - 已驳回、已取消、已完成的预约不能取消

3. **权限验证**：
   - 所有需要权限的接口都会验证 Token
   - 管理员接口需要 role=1 或 role=2
   - 超级管理员接口需要 role=2

4. **数据完整性**：
   - 使用外键约束保证数据完整性
   - 删除用户或会议室时，相关预约记录会被级联删除

---

## 📄 许可证

本项目采用 MIT 许可证。

---

## 👥 贡献

欢迎提交 Issue 和 Pull Request！

---

## 📮 联系方式

如有问题，请查看项目文档或联系开发团队。

---

**最后更新**：2025-12-16

