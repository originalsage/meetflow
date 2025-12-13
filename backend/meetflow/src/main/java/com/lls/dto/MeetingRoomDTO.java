package com.lls.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 会议室DTO
 */
@Data
public class MeetingRoomDTO {
    private Long id;

    @NotBlank(message = "会议室名称不能为空")
    private String name;

    @NotBlank(message = "房号不能为空")
    private String roomNumber;

    @NotNull(message = "容纳人数不能为空")
    @Positive(message = "容纳人数必须大于0")
    private Integer capacity;

    private BigDecimal area;

    private String purpose;

    private String photoUrl;

    private Integer status; // 0-不可预约，1-可预约
}

