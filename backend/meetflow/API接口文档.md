# 会议室预约系统 API 接口文档

## 基础信息

- **Base URL**: `http://localhost:8080/api`
- **认证方式**: JWT Token（Bearer Token）
- **请求头**: `Authorization: Bearer {token}`

## 统一响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

## 一、认证模块

### 1.1 用户登录
- **URL**: `/auth/login`
- **Method**: `POST`
- **Auth**: 不需要
- **Request Body**:
```json
{
  "username": "admin",
  "password": "admin123",
  "role": 1
}
```
- **Response**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userInfo": {
      "id": 1,
      "username": "admin",
      "name": "系统管理员",
      "role": 1,
      "phone": "13800138000",
      "email": "admin@example.com"
    }
  }
}
```

### 1.2 获取当前用户信息
- **URL**: `/auth/info`
- **Method**: `GET`
- **Auth**: 需要
- **Response**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "admin",
    "name": "系统管理员",
    "role": 1
  }
}
```

## 二、会议室模块

### 2.1 获取所有会议室列表
- **URL**: `/meeting-rooms`
- **Method**: `GET`
- **Auth**: 不需要
- **Response**:
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "name": "大型会议室A",
      "roomNumber": "A101",
      "capacity": 50,
      "area": 100.00,
      "purpose": "适用于大型会议",
      "photoUrl": "https://...",
      "status": 1
    }
  ]
}
```

### 2.2 获取会议室详情
- **URL**: `/meeting-rooms/{id}`
- **Method**: `GET`
- **Auth**: 不需要

### 2.3 根据条件筛选可用会议室
- **URL**: `/meeting-rooms/available`
- **Method**: `GET`
- **Auth**: 不需要
- **Query Parameters**:
  - `date`: 预约日期 (格式: yyyy-MM-dd)
  - `startTime`: 开始时间 (0-23)
  - `endTime`: 结束时间 (0-23)
  - `attendeeCount`: 参会人数
- **Example**: `/meeting-rooms/available?date=2024-01-15&startTime=9&endTime=11&attendeeCount=20`

### 2.4 添加会议室（管理员）
- **URL**: `/meeting-rooms`
- **Method**: `POST`
- **Auth**: 需要（管理员）
- **Request Body**:
```json
{
  "name": "中型会议室B",
  "roomNumber": "B201",
  "capacity": 20,
  "area": 50.00,
  "purpose": "适用于部门会议",
  "status": 1
}
```

### 2.5 修改会议室（管理员）
- **URL**: `/meeting-rooms/{id}`
- **Method**: `PUT`
- **Auth**: 需要（管理员）

### 2.6 删除会议室（管理员）
- **URL**: `/meeting-rooms/{id}`
- **Method**: `DELETE`
- **Auth**: 需要（管理员）

### 2.7 设置会议室状态（管理员）
- **URL**: `/meeting-rooms/{id}/status`
- **Method**: `PUT`
- **Auth**: 需要（管理员）
- **Query Parameters**:
  - `status`: 0-不可预约，1-可预约

### 2.8 上传会议室照片（管理员）
- **URL**: `/meeting-rooms/upload`
- **Method**: `POST`
- **Auth**: 需要（管理员）
- **Content-Type**: `multipart/form-data`
- **Request**: `file` (文件)

## 三、预约模块

### 3.1 创建预约
- **URL**: `/reservations`
- **Method**: `POST`
- **Auth**: 需要
- **Request Body**:
```json
{
  "meetingRoomId": 1,
  "meetingTitle": "项目讨论会",
  "reservationDate": "2024-01-15",
  "startTime": 9,
  "endTime": 11,
  "attendeeCount": 15
}
```

### 3.2 查询我的预约记录
- **URL**: `/reservations/my`
- **Method**: `GET`
- **Auth**: 需要
- **Query Parameters**:
  - `status`: 可选，状态筛选 (0-待审批，1-已通过，2-已驳回，3-已取消，4-已完成)

### 3.3 获取预约详情
- **URL**: `/reservations/{id}`
- **Method**: `GET`
- **Auth**: 不需要

### 3.4 取消预约
- **URL**: `/reservations/{id}/cancel`
- **Method**: `PUT`
- **Auth**: 需要

### 3.5 查询所有预约记录（管理员）
- **URL**: `/reservations/all`
- **Method**: `GET`
- **Auth**: 需要（管理员）
- **Query Parameters**:
  - `status`: 可选，状态筛选

### 3.6 审批预约（管理员）
- **URL**: `/reservations/{id}/approve`
- **Method**: `PUT`
- **Auth**: 需要（管理员）
- **Request Body** (通过):
```json
{}
```
- **Request Body** (驳回):
```json
{
  "rejectReason": "该时间段已被占用"
}
```

### 3.7 获取一周预约情况（管理员）
- **URL**: `/reservations/weekly`
- **Method**: `GET`
- **Auth**: 需要（管理员）
- **Query Parameters**:
  - `weekStartDate`: 周开始日期 (格式: yyyy-MM-dd)

### 3.8 获取当天各时段占用情况（管理员）
- **URL**: `/reservations/daily`
- **Method**: `GET`
- **Auth**: 需要（管理员）
- **Query Parameters**:
  - `date`: 日期 (格式: yyyy-MM-dd)

## 状态码说明

### 预约状态
- `0`: 待审批
- `1`: 已通过
- `2`: 已驳回
- `3`: 已取消
- `4`: 已完成

### 会议室状态
- `0`: 不可预约
- `1`: 可预约

### 用户角色
- `0`: 普通用户
- `1`: 管理员

## 错误码说明

- `200`: 操作成功
- `401`: 未登录或登录已过期
- `403`: 无权限访问
- `500`: 操作失败
- `1001`: 用户不存在
- `1002`: 用户名或密码错误
- `2001`: 会议室不存在
- `2002`: 房号已存在
- `2003`: 会议室不可预约
- `2004`: 会议室容量不足
- `3001`: 预约记录不存在
- `3002`: 该时间段已被预约
- `3003`: 预约时间无效
- `4001`: 参数错误

