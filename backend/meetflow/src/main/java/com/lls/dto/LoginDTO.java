package com.lls.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录DTO
 */
@Data
public class LoginDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    private Integer role; // 0-普通用户，1-管理员
}

