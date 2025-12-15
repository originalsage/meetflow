# Apifox 导入 API 文档说明

## 导入步骤

1. 打开 Apifox
2. 选择项目 → **导入** → **OpenAPI/Swagger**
3. 选择文件 `api-docs-openapi.yaml`
4. 点击导入

## 测试前准备

### 1. 启动后端服务
确保 Spring Boot 应用已启动，运行在 `http://localhost:8080`

### 2. 配置环境变量
在 Apifox 中设置环境变量：
- **baseUrl**: `http://localhost:8080/api`

### 3. 配置认证
1. 先调用登录接口获取 Token
2. 在 Apifox 的环境变量中设置 `token` 变量
3. 在需要认证的接口中，设置 Authorization Header：
   - 类型：Bearer Token
   - Token：`{{token}}`

## 测试流程建议

### 第一步：登录获取 Token
1. 调用 `POST /auth/login`
   - 管理员登录：
     ```json
     {
       "username": "admin",
       "password": "admin123",
       "role": 1
     }
     ```
   - 普通用户登录：
     ```json
     {
       "username": "user001",
       "password": "user123",
       "role": 0
     }
     ```
2. 从响应中复制 `token` 值
3. 在 Apifox 环境变量中设置 `token`

### 第二步：测试会议室接口
1. `GET /meeting-rooms` - 获取所有会议室
2. `GET /meeting-rooms/{id}` - 获取会议室详情
3. `GET /meeting-rooms/available` - 筛选可用会议室
   - 参数：`date=2024-01-15&startTime=9&endTime=11&attendeeCount=20`

### 第三步：测试预约接口（需要登录）
1. `POST /reservations` - 创建预约
2. `GET /reservations/my` - 查询我的预约
3. `GET /reservations/{id}` - 获取预约详情
4. `PUT /reservations/{id}/cancel` - 取消预约

### 第四步：测试管理员接口（需要管理员Token）
1. `POST /meeting-rooms` - 添加会议室
2. `POST /meeting-rooms/upload` - 上传照片
3. `GET /reservations/all` - 查询所有预约
4. `PUT /reservations/{id}/approve` - 审批预约
5. `GET /reservations/weekly` - 一周预约情况
6. `GET /reservations/daily` - 当天时段占用情况

## 常见问题

### Token 过期
如果返回 401 错误，重新登录获取新 Token

### 权限不足
确保使用管理员账号登录（role=1）才能访问管理员接口

### 时间冲突
创建预约时如果返回"该时间段已被预约"，说明该时间段已有其他预约

## 测试数据

根据建表SQL，默认测试账号：
- **管理员**: username=`admin`, password=`admin123` (需要BCrypt加密后的密码)
- **普通用户**: username=`user001`, password=`user123` (需要BCrypt加密后的密码)
