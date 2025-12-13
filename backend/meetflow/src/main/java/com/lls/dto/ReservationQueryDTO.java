package com.lls.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 预约查询DTO（用于筛选可用会议室）
 */
@Data
public class ReservationQueryDTO {
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;
    private Integer startTime;
    private Integer endTime;
    private Integer attendeeCount;
}

