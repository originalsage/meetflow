package com.lls.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应状态码枚举
 */
@Getter
@AllArgsConstructor
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),
    
    // 认证相关
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    
    // 业务相关
    USER_NOT_FOUND(1001, "用户不存在"),
    USERNAME_OR_PASSWORD_ERROR(1002, "用户名或密码错误"),
    USERNAME_ALREADY_EXISTS(1003, "用户名已存在"),
    
    MEETING_ROOM_NOT_FOUND(2001, "会议室不存在"),
    MEETING_ROOM_NUMBER_EXISTS(2002, "房号已存在"),
    MEETING_ROOM_NOT_AVAILABLE(2003, "会议室不可预约"),
    MEETING_ROOM_CAPACITY_INSUFFICIENT(2004, "会议室容量不足"),
    
    RESERVATION_NOT_FOUND(3001, "预约记录不存在"),
    RESERVATION_TIME_CONFLICT(3002, "该时间段已被预约"),
    RESERVATION_TIME_INVALID(3003, "预约时间无效"),
    RESERVATION_ALREADY_APPROVED(3004, "预约已审批，无法取消"),
    RESERVATION_ALREADY_CANCELLED(3005, "预约已取消"),
    RESERVATION_STATUS_INVALID(3006, "预约状态无效"),
    RESERVATION_USER_TIME_CONFLICT(3007, "您在该时间段已有其他预约，不能同时预约多个会议室"),
    
    PARAM_ERROR(4001, "参数错误"),
    FILE_UPLOAD_ERROR(4002, "文件上传失败");

    private final Integer code;
    private final String message;
}

