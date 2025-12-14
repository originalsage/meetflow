package com.lls.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 降级用户权限DTO
 */
@Data
public class DemoteUserDTO {
    @NotNull(message = "用户ID不能为空")
    private Long userId;
}

