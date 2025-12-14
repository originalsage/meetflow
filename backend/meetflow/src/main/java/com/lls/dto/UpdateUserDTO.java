package com.lls.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 更新用户信息DTO
 */
@Data
public class UpdateUserDTO {
    @NotBlank(message = "真实姓名不能为空")
    private String name;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 旧密码（修改密码时需要）
     */
    private String oldPassword;

    /**
     * 新密码（修改密码时需要）
     */
    @Pattern(regexp = "^.{6,20}$", message = "密码长度必须在6-20位之间")
    private String newPassword;

    /**
     * 确认新密码（修改密码时需要）
     */
    private String confirmPassword;
}

