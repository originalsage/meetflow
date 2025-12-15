# 会议室预约系统 API 接口文档

## 基础信息

- **Base URL**: `http://localhost:8080/api`
- **认证方式**: JWT Token（Bearer Token）
- **请求头**: `Authorization: Bearer {token}`
- **Content-Type**: `application/json`（文件上传接口除外）

## 统一响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

### 响应字段说明

- `code`: 响应状态码（200表示成功，其他表示失败）
- `message`: 响应消息
- `data`: 响应数据（根据接口不同而不同）

---

## 一、认证模块 (Auth)

### 1.1 用户注册

- **URL**: `/auth/register`
- **Method**: `POST`
- **Auth**: 不需要
- **Description**: 用户注册新账号
- **Request Body**:
```json
{
  "username": "user001",
  "password": "123456",
  "confirmPassword": "123456",
  "name": "张三",
  "phone": "13800138000",
  "email": "zhangsan@example.com"
}
```

**字段说明**:
- `username`: 用户名（必填，不能为空）
- `password`: 密码（必填，不能为空）
- `confirmPassword`: 确认密码（必填，必须与password一致）
- `name`: 真实姓名（必填，不能为空）
- `phone`: 手机号（可选，格式：1[3-9]开头的11位数字）
- `email`: 邮箱（可选，必须符合邮箱格式）

- **Response**:
```json
{
  "code": 200,
  "message": "注册成功",
  "data": null
}
```

---

### 1.2 用户登录

- **URL**: `/auth/login`
- **Method**: `POST`
- **Auth**: 不需要
- **Description**: 用户登录，返回JWT Token和用户信息
- **Request Body**:
```json
{
  "username": "admin",
  "password": "admin123",
  "role": 1
}
```

**字段说明**:
- `username`: 用户名（必填）
- `password`: 密码（必填）
- `role`: 角色（可选，0-普通用户，1-管理员，2-超级管理员）

- **Response**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userInfo": {
      "id": 1,
      "username": "admin",
      "name": "系统管理员",
      "role": 1,
      "phone": "13800138000",
      "email": "admin@example.com",
      "createTime": "2025-01-01T10:00:00"
    }
  }
}
```

---

### 1.3 获取当前用户信息

- **URL**: `/auth/info`
- **Method**: `GET`
- **Auth**: 需要
- **Description**: 获取当前登录用户的详细信息
- **Response**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "admin",
    "name": "系统管理员",
    "role": 1,
    "phone": "13800138000",
    "email": "admin@example.com",
    "createTime": "2025-01-01T10:00:00"
  }
}
```

---

### 1.4 更新用户信息

- **URL**: `/auth/update`
- **Method**: `PUT`
- **Auth**: 需要
- **Description**: 更新当前登录用户的信息，包括修改密码
- **Request Body**:
```json
{
  "name": "张三",
  "phone": "13800138000",
  "email": "zhangsan@example.com",
  "oldPassword": "123456",
  "newPassword": "newpass123",
  "confirmPassword": "newpass123"
}
```

**字段说明**:
- `name`: 真实姓名（必填）
- `phone`: 手机号（可选，格式：1[3-9]开头的11位数字）
- `email`: 邮箱（可选，必须符合邮箱格式）
- `oldPassword`: 旧密码（修改密码时必填）
- `newPassword`: 新密码（修改密码时必填，长度6-20位）
- `confirmPassword`: 确认新密码（修改密码时必填，必须与newPassword一致）

- **Response**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 1,
    "username": "admin",
    "name": "张三",
    "role": 1,
    "phone": "13800138000",
    "email": "zhangsan@example.com",
    "createTime": "2025-01-01T10:00:00"
  }
}
```

---

## 二、用户管理模块 (Users)

> **注意**: 以下接口仅超级管理员（role=2）可访问

### 2.1 获取所有用户列表

- **URL**: `/users`
- **Method**: `GET`
- **Auth**: 需要（超级管理员）
- **Description**: 获取系统中所有用户列表
- **Response**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "username": "admin",
      "name": "系统管理员",
      "role": 2,
      "phone": "13800138000",
      "email": "admin@example.com",
      "createTime": "2025-01-01T10:00:00"
    }
  ]
}
```

---

### 2.2 删除用户

- **URL**: `/users/{id}`
- **Method**: `DELETE`
- **Auth**: 需要（超级管理员）
- **Description**: 删除指定用户
- **Path Parameters**:
  - `id`: 用户ID（必填）

- **Response**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

### 2.3 提升用户权限

- **URL**: `/users/promote`
- **Method**: `PUT`
- **Auth**: 需要（超级管理员）
- **Description**: 提升用户权限为普通管理员（role=1）
- **Request Body**:
```json
{
  "userId": 2,
  "role": 1
}
```

**字段说明**:
- `userId`: 用户ID（必填）
- `role`: 角色（必填，只能设置为1-普通管理员）

- **Response**:
```json
{
  "code": 200,
  "message": "提升权限成功",
  "data": null
}
```

---

### 2.4 降级用户权限

- **URL**: `/users/demote`
- **Method**: `PUT`
- **Auth**: 需要（超级管理员）
- **Description**: 将管理员降级为普通用户（role=0）
- **Request Body**:
```json
{
  "userId": 2
}
```

**字段说明**:
- `userId`: 用户ID（必填）

- **Response**:
```json
{
  "code": 200,
  "message": "降级权限成功",
  "data": null
}
```

---

## 三、会议室模块 (Meeting Rooms)

### 3.1 获取所有会议室列表

- **URL**: `/meeting-rooms`
- **Method**: `GET`
- **Auth**: 不需要
- **Description**: 获取系统中所有会议室信息
- **Response**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "name": "大型会议室A",
      "roomNumber": "A101",
      "capacity": 50,
      "area": 100.00,
      "purpose": "适用于大型会议",
      "photoUrl": "https://example.com/photo.jpg",
      "status": 1,
      "createTime": "2025-01-01T10:00:00"
    }
  ]
}
```

---

### 3.2 获取会议室详情

- **URL**: `/meeting-rooms/{id}`
- **Method**: `GET`
- **Auth**: 不需要
- **Description**: 根据ID获取会议室详细信息
- **Path Parameters**:
  - `id`: 会议室ID（必填）

- **Response**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "name": "大型会议室A",
    "roomNumber": "A101",
    "capacity": 50,
    "area": 100.00,
    "purpose": "适用于大型会议",
    "photoUrl": "https://example.com/photo.jpg",
    "status": 1,
    "createTime": "2025-01-01T10:00:00"
  }
}
```

---

### 3.3 根据条件筛选可用会议室

- **URL**: `/meeting-rooms/available`
- **Method**: `GET`
- **Auth**: 不需要
- **Description**: 根据日期、时间段、参会人数等条件筛选可用会议室
- **Query Parameters**:
  - `date`: 预约日期（可选，格式: yyyy-MM-dd）
  - `startTime`: 开始时间（可选，0-23）
  - `endTime`: 结束时间（可选，0-23）
  - `attendeeCount`: 参会人数（可选）

- **Example**: `/meeting-rooms/available?date=2025-01-15&startTime=9&endTime=11&attendeeCount=20`

- **Response**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "name": "大型会议室A",
      "roomNumber": "A101",
      "capacity": 50,
      "area": 100.00,
      "purpose": "适用于大型会议",
      "photoUrl": "https://example.com/photo.jpg",
      "status": 1
    }
  ]
}
```

---

### 3.4 添加会议室

- **URL**: `/meeting-rooms`
- **Method**: `POST`
- **Auth**: 需要（管理员或超级管理员）
- **Description**: 添加新会议室
- **Request Body**:
```json
{
  "name": "中型会议室B",
  "roomNumber": "B201",
  "capacity": 20,
  "area": 50.00,
  "purpose": "适用于部门会议",
  "photoUrl": "https://example.com/photo.jpg",
  "status": 1
}
```

**字段说明**:
- `name`: 会议室名称（必填）
- `roomNumber`: 房间号（必填，唯一）
- `capacity`: 容量（必填，整数）
- `area`: 面积（可选，浮点数）
- `purpose`: 用途说明（可选）
- `photoUrl`: 照片URL（可选）
- `status`: 状态（可选，0-不可预约，1-可预约，默认1）

- **Response**:
```json
{
  "code": 200,
  "message": "添加成功",
  "data": {
    "id": 2,
    "name": "中型会议室B",
    "roomNumber": "B201",
    "capacity": 20,
    "area": 50.00,
    "purpose": "适用于部门会议",
    "photoUrl": "https://example.com/photo.jpg",
    "status": 1,
    "createTime": "2025-01-15T10:00:00"
  }
}
```

---

### 3.5 修改会议室

- **URL**: `/meeting-rooms/{id}`
- **Method**: `PUT`
- **Auth**: 需要（管理员或超级管理员）
- **Description**: 修改会议室信息
- **Path Parameters**:
  - `id`: 会议室ID（必填）

- **Request Body**: 同添加会议室接口

- **Response**:
```json
{
  "code": 200,
  "message": "修改成功",
  "data": {
    "id": 2,
    "name": "中型会议室B（已修改）",
    "roomNumber": "B201",
    "capacity": 25,
    "area": 60.00,
    "purpose": "适用于部门会议",
    "photoUrl": "https://example.com/photo.jpg",
    "status": 1,
    "createTime": "2025-01-15T10:00:00"
  }
}
```

---

### 3.6 删除会议室

- **URL**: `/meeting-rooms/{id}`
- **Method**: `DELETE`
- **Auth**: 需要（管理员或超级管理员）
- **Description**: 删除指定会议室
- **Path Parameters**:
  - `id`: 会议室ID（必填）

- **Response**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

### 3.7 设置会议室状态

- **URL**: `/meeting-rooms/{id}/status`
- **Method**: `PUT`
- **Auth**: 需要（管理员或超级管理员）
- **Description**: 设置会议室是否可预约
- **Path Parameters**:
  - `id`: 会议室ID（必填）

- **Query Parameters**:
  - `status`: 状态（必填，0-不可预约，1-可预约）

- **Example**: `/meeting-rooms/1/status?status=0`

- **Response**:
```json
{
  "code": 200,
  "message": "设置成功",
  "data": null
}
```

---

### 3.8 上传会议室照片

- **URL**: `/meeting-rooms/upload`
- **Method**: `POST`
- **Auth**: 需要（管理员或超级管理员）
- **Description**: 上传会议室照片
- **Content-Type**: `multipart/form-data`
- **Request Body**: 
  - `file`: 图片文件（必填）

- **Response**:
```json
{
  "code": 200,
  "message": "上传成功",
  "data": "https://example.com/uploaded-photo.jpg"
}
```

---

## 四、预约模块 (Reservations)

### 4.1 创建预约

- **URL**: `/reservations`
- **Method**: `POST`
- **Auth**: 需要
- **Description**: 创建新的会议室预约
- **Request Body**:
```json
{
  "meetingRoomId": 1,
  "meetingTitle": "项目讨论会",
  "reservationDate": "2025-01-15",
  "startTime": 9,
  "endTime": 11,
  "attendeeCount": 15
}
```

**字段说明**:
- `meetingRoomId`: 会议室ID（必填）
- `meetingTitle`: 会议标题（必填）
- `reservationDate`: 预约日期（必填，格式: yyyy-MM-dd）
- `startTime`: 开始时间（必填，0-23）
- `endTime`: 结束时间（必填，0-23，必须大于startTime）
- `attendeeCount`: 参会人数（必填，整数）

- **Response**:
```json
{
  "code": 200,
  "message": "预约成功",
  "data": {
    "id": 1,
    "userId": 2,
    "userName": "张三",
    "meetingRoomId": 1,
    "meetingRoomName": "大型会议室A",
    "roomNumber": "A101",
    "meetingTitle": "项目讨论会",
    "reservationDate": "2025-01-15",
    "startTime": 9,
    "endTime": 11,
    "attendeeCount": 15,
    "status": 0,
    "createTime": "2025-01-14T10:00:00"
  }
}
```

---

### 4.2 查询我的预约记录

- **URL**: `/reservations/my`
- **Method**: `GET`
- **Auth**: 需要
- **Description**: 查询当前用户的所有预约记录
- **Query Parameters**:
  - `status`: 状态筛选（可选，0-待审批，1-已通过，2-已驳回，3-已取消，4-已完成）

- **Example**: `/reservations/my?status=1`

- **Response**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "userId": 2,
      "userName": "张三",
      "meetingRoomId": 1,
      "meetingRoomName": "大型会议室A",
      "roomNumber": "A101",
      "meetingTitle": "项目讨论会",
      "reservationDate": "2025-01-15",
      "startTime": 9,
      "endTime": 11,
      "attendeeCount": 15,
      "status": 1,
      "approveTime": "2025-01-14T11:00:00",
      "approverId": 1,
      "approverName": "管理员",
      "createTime": "2025-01-14T10:00:00",
      "photoUrl": "https://example.com/photo.jpg"
    }
  ]
}
```

---

### 4.3 分页查询我的预约记录

- **URL**: `/reservations/my/page`
- **Method**: `GET`
- **Auth**: 需要
- **Description**: 分页查询当前用户的预约记录
- **Query Parameters**:
  - `status`: 状态筛选（可选，0-待审批，1-已通过，2-已驳回，3-已取消，4-已完成）
  - `page`: 页码（可选，默认1）
  - `pageSize`: 每页大小（可选，默认12）

- **Example**: `/reservations/my/page?status=1&page=1&pageSize=10`

- **Response**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "userId": 2,
        "userName": "张三",
        "meetingRoomId": 1,
        "meetingRoomName": "大型会议室A",
        "roomNumber": "A101",
        "meetingTitle": "项目讨论会",
        "reservationDate": "2025-01-15",
        "startTime": 9,
        "endTime": 11,
        "attendeeCount": 15,
        "status": 1,
        "createTime": "2025-01-14T10:00:00"
      }
    ],
    "total": 50,
    "current": 1,
    "size": 12,
    "pages": 5
  }
}
```

---

### 4.4 获取预约详情

- **URL**: `/reservations/{id}`
- **Method**: `GET`
- **Auth**: 不需要
- **Description**: 根据ID获取预约详情
- **Path Parameters**:
  - `id`: 预约ID（必填）

- **Response**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "userId": 2,
    "userName": "张三",
    "meetingRoomId": 1,
    "meetingRoomName": "大型会议室A",
    "roomNumber": "A101",
    "meetingTitle": "项目讨论会",
    "reservationDate": "2025-01-15",
    "startTime": 9,
    "endTime": 11,
    "attendeeCount": 15,
    "status": 1,
    "rejectReason": null,
    "approveTime": "2025-01-14T11:00:00",
    "approverId": 1,
    "approverName": "管理员",
    "createTime": "2025-01-14T10:00:00",
    "photoUrl": "https://example.com/photo.jpg"
  }
}
```

---

### 4.5 取消预约

- **URL**: `/reservations/{id}/cancel`
- **Method**: `PUT`
- **Auth**: 需要
- **Description**: 取消自己的预约（只能取消待审批或已通过的预约）
- **Path Parameters**:
  - `id`: 预约ID（必填）

- **Response**:
```json
{
  "code": 200,
  "message": "取消成功",
  "data": null
}
```

---

### 4.6 查询所有预约记录

- **URL**: `/reservations/all`
- **Method**: `GET`
- **Auth**: 需要（管理员或超级管理员）
- **Description**: 查询系统中所有预约记录
- **Query Parameters**:
  - `status`: 状态筛选（可选，0-待审批，1-已通过，2-已驳回，3-已取消，4-已完成）

- **Example**: `/reservations/all?status=0`

- **Response**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "userId": 2,
      "userName": "张三",
      "meetingRoomId": 1,
      "meetingRoomName": "大型会议室A",
      "roomNumber": "A101",
      "meetingTitle": "项目讨论会",
      "reservationDate": "2025-01-15",
      "startTime": 9,
      "endTime": 11,
      "attendeeCount": 15,
      "status": 0,
      "createTime": "2025-01-14T10:00:00"
    }
  ]
}
```

---

### 4.7 分页查询所有预约记录

- **URL**: `/reservations/all/page`
- **Method**: `GET`
- **Auth**: 需要（管理员或超级管理员）
- **Description**: 分页查询系统中所有预约记录
- **Query Parameters**:
  - `status`: 状态筛选（可选，0-待审批，1-已通过，2-已驳回，3-已取消，4-已完成）
  - `page`: 页码（可选，默认1）
  - `pageSize`: 每页大小（可选，默认12）

- **Example**: `/reservations/all/page?status=0&page=1&pageSize=10`

- **Response**: 同分页查询我的预约记录接口

---

### 4.8 审批预约

- **URL**: `/reservations/{id}/approve`
- **Method**: `PUT`
- **Auth**: 需要（管理员或超级管理员）
- **Description**: 审批预约，可以批准或驳回
- **Path Parameters**:
  - `id`: 预约ID（必填）

- **Request Body** (通过):
```json
{}
```
或
```json
{
  "rejectReason": ""
}
```

- **Request Body** (驳回):
```json
{
  "rejectReason": "该时间段已被占用"
}
```

**字段说明**:
- `rejectReason`: 驳回理由（可选，如果提供且不为空则驳回，否则通过）

- **Response**:
```json
{
  "code": 200,
  "message": "审批成功",
  "data": null
}
```

---

### 4.9 获取一周预约情况

- **URL**: `/reservations/weekly`
- **Method**: `GET`
- **Auth**: 需要（管理员或超级管理员）
- **Description**: 获取指定周的所有预约情况。该接口会返回指定周内所有状态为"待审批"（status=0）、"已通过"（status=1）和"已完成"（status=4）的预约记录，用于在每周预约情况页面展示完整的会议室使用情况。
- **Query Parameters**:
  - `weekStartDate`: 周开始日期（必填，格式: yyyy-MM-dd，通常为周一）

- **Example**: `/reservations/weekly?weekStartDate=2025-01-15`

- **Response**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "userId": 2,
      "userName": "张三",
      "meetingRoomId": 1,
      "meetingRoomName": "大型会议室A",
      "roomNumber": "A101",
      "meetingTitle": "项目讨论会",
      "reservationDate": "2025-01-15",
      "startTime": 9,
      "endTime": 11,
      "attendeeCount": 15,
      "status": 1,
      "createTime": "2025-01-14T10:00:00"
    },
    {
      "id": 2,
      "userId": 3,
      "userName": "李四",
      "meetingRoomId": 1,
      "meetingRoomName": "大型会议室A",
      "roomNumber": "A101",
      "meetingTitle": "部门例会",
      "reservationDate": "2025-01-15",
      "startTime": 14,
      "endTime": 16,
      "attendeeCount": 20,
      "status": 4,
      "createTime": "2025-01-14T08:00:00"
    }
  ]
}
```

**注意事项**:
- 该接口只返回状态为待审批（0）、已通过（1）和已完成（4）的预约
- 已驳回（2）和已取消（3）的预约不会出现在结果中
- 结果按预约日期和开始时间升序排列

---

### 4.10 获取当天各时段占用情况

- **URL**: `/reservations/daily`
- **Method**: `GET`
- **Auth**: 需要（管理员或超级管理员）
- **Description**: 获取指定日期各时段的会议室占用情况。该接口会返回指定日期内所有状态为"待审批"（status=0）、"已通过"（status=1）和"已完成"（status=4）的预约记录，用于在每日预约情况页面展示完整的会议室使用情况。
- **Query Parameters**:
  - `date`: 日期（必填，格式: yyyy-MM-dd）

- **Example**: `/reservations/daily?date=2025-01-15`

- **Response**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "userId": 2,
      "userName": "张三",
      "meetingRoomId": 1,
      "meetingRoomName": "大型会议室A",
      "roomNumber": "A101",
      "meetingTitle": "项目讨论会",
      "reservationDate": "2025-01-15",
      "startTime": 9,
      "endTime": 11,
      "attendeeCount": 15,
      "status": 1,
      "createTime": "2025-01-14T10:00:00"
    },
    {
      "id": 2,
      "userId": 3,
      "userName": "李四",
      "meetingRoomId": 2,
      "meetingRoomName": "中型会议室B",
      "roomNumber": "B201",
      "meetingTitle": "部门例会",
      "reservationDate": "2025-01-15",
      "startTime": 14,
      "endTime": 16,
      "attendeeCount": 20,
      "status": 4,
      "createTime": "2025-01-14T08:00:00"
    }
  ]
}
```

**注意事项**:
- 该接口只返回状态为待审批（0）、已通过（1）和已完成（4）的预约
- 已驳回（2）和已取消（3）的预约不会出现在结果中
- 结果按开始时间升序排列

---

### 4.11 获取用户当前会议室使用状态

- **URL**: `/reservations/current-usage`
- **Method**: `GET`
- **Auth**: 需要
- **Description**: 获取当前用户正在使用或即将使用的会议室状态
- **Response**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "hasCurrentUsage": true,
    "reservation": {
      "id": 1,
      "userId": 2,
      "userName": "张三",
      "meetingRoomId": 1,
      "meetingRoomName": "大型会议室A",
      "roomNumber": "A101",
      "meetingTitle": "项目讨论会",
      "reservationDate": "2025-01-15",
      "startTime": 9,
      "endTime": 11,
      "attendeeCount": 15,
      "status": 1
    },
    "status": "ongoing",
    "startDateTime": "2025-01-15T09:00:00",
    "endDateTime": "2025-01-15T11:00:00",
    "timeDiffSeconds": 3600,
    "nextReservation": null
  }
}
```

**字段说明**:
- `hasCurrentUsage`: 是否有当前使用中的会议室
- `reservation`: 当前预约信息（如果有）
- `status`: 状态（pending-待开始，ongoing-进行中，ended-已结束）
- `startDateTime`: 会议开始时间
- `endDateTime`: 会议结束时间
- `timeDiffSeconds`: 距离开始/已进行/已结束的时间（秒）
- `nextReservation`: 下一个待使用的会议室（如果没有当前使用）

---

### 4.12 完成预约（确认使用）

- **URL**: `/reservations/{id}/complete`
- **Method**: `PUT`
- **Auth**: 需要
- **Description**: 确认使用会议室，将预约状态更新为已完成（status=4）。只能在会议进行中且预约状态为已通过时使用。
- **Path Parameters**:
  - `id`: 预约ID（必填）

- **Response**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

**注意事项**:
- 只能完成自己的预约
- 只能完成状态为"已通过"（status=1）的预约
- 建议在会议进行中时使用此功能

---

## 五、状态码说明

### 5.1 预约状态

- `0`: 待审批
- `1`: 已通过
- `2`: 已驳回
- `3`: 已取消
- `4`: 已完成

### 5.2 会议室状态

- `0`: 不可预约
- `1`: 可预约

### 5.3 用户角色

- `0`: 普通用户
- `1`: 普通管理员
- `2`: 超级管理员

---

## 六、错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 401 | 未登录或登录已过期 |
| 403 | 无权限访问 |
| 500 | 操作失败 |
| 1001 | 用户不存在 |
| 1002 | 用户名或密码错误 |
| 1003 | 用户名已存在 |
| 1004 | 密码错误（修改密码时） |
| 2001 | 会议室不存在 |
| 2002 | 房号已存在 |
| 2003 | 会议室不可预约 |
| 2004 | 会议室容量不足 |
| 3001 | 预约记录不存在 |
| 3002 | 该时间段已被预约 |
| 3003 | 预约时间无效 |
| 3004 | 只能取消待审批或已通过的预约 |
| 4001 | 参数错误 |
| 4002 | 验证失败 |

---

## 七、权限说明

### 7.1 接口权限分类

1. **公开接口**（无需认证）:
   - 用户注册
   - 用户登录
   - 获取所有会议室列表
   - 获取会议室详情
   - 根据条件筛选可用会议室
   - 获取预约详情

2. **普通用户接口**（需要登录）:
   - 获取当前用户信息
   - 更新用户信息
   - 创建预约
   - 查询我的预约记录
   - 分页查询我的预约记录
   - 取消预约
   - 获取用户当前会议室使用状态

3. **管理员接口**（需要管理员或超级管理员权限，role=1或2）:
   - 添加会议室
   - 修改会议室
   - 删除会议室
   - 设置会议室状态
   - 上传会议室照片
   - 查询所有预约记录
   - 分页查询所有预约记录
   - 审批预约
   - 获取一周预约情况
   - 获取当天各时段占用情况

4. **超级管理员接口**（需要超级管理员权限，role=2）:
   - 获取所有用户列表
   - 删除用户
   - 提升用户权限
   - 降级用户权限

---

## 八、使用示例

### 8.1 完整流程示例

#### 1. 用户注册
```bash
POST /api/auth/register
Content-Type: application/json

{
  "username": "user001",
  "password": "123456",
  "confirmPassword": "123456",
  "name": "张三",
  "phone": "13800138000",
  "email": "zhangsan@example.com"
}
```

#### 2. 用户登录
```bash
POST /api/auth/login
Content-Type: application/json

{
  "username": "user001",
  "password": "123456"
}
```

响应中获取 `token`，后续请求需要在 Header 中携带：
```
Authorization: Bearer {token}
```

#### 3. 查询可用会议室
```bash
GET /api/meeting-rooms/available?date=2025-01-15&startTime=9&endTime=11&attendeeCount=20
```

#### 4. 创建预约
```bash
POST /api/reservations
Authorization: Bearer {token}
Content-Type: application/json

{
  "meetingRoomId": 1,
  "meetingTitle": "项目讨论会",
  "reservationDate": "2025-01-15",
  "startTime": 9,
  "endTime": 11,
  "attendeeCount": 15
}
```

#### 5. 管理员审批预约
```bash
PUT /api/reservations/1/approve
Authorization: Bearer {admin_token}
Content-Type: application/json

{}
```

---

## 九、注意事项

1. **Token 有效期**: JWT Token 默认有效期为24小时，过期后需要重新登录
2. **时间格式**: 
   - 日期格式：`yyyy-MM-dd`（如：2025-01-15）
   - 日期时间格式：`yyyy-MM-ddTHH:mm:ss`（如：2025-01-15T10:00:00）
3. **时间段**: 开始时间和结束时间使用0-23的整数表示小时
4. **文件上传**: 上传会议室照片时，文件大小建议不超过5MB，支持常见图片格式（jpg、png、gif等）
5. **分页参数**: 分页查询接口中，`page` 从1开始，`pageSize` 默认12
6. **权限验证**: 所有需要权限的接口都会验证Token，如果Token无效或权限不足会返回401或403错误
