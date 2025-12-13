package com.lls.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.time.LocalDate;

/**
 * 预约DTO
 */
@Data
public class ReservationDTO {
    private Long id;

    @NotNull(message = "会议室ID不能为空")
    private Long meetingRoomId;

    @NotBlank(message = "会议主题不能为空")
    private String meetingTitle;

    @NotNull(message = "预约日期不能为空")
    private LocalDate reservationDate;

    @NotNull(message = "开始时间不能为空")
    private Integer startTime; // 0-23

    @NotNull(message = "结束时间不能为空")
    private Integer endTime; // 0-23

    @NotNull(message = "参会人数不能为空")
    @Positive(message = "参会人数必须大于0")
    private Integer attendeeCount;
}

