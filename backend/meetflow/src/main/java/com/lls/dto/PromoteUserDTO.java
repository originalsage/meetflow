package com.lls.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 提升用户权限DTO
 */
@Data
public class PromoteUserDTO {
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "角色不能为空")
    private Integer role; // 只能设置为1（普通管理员）
}

