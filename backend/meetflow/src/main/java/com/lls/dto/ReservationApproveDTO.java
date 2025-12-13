package com.lls.dto;

import lombok.Data;

/**
 * 预约审批DTO
 */
@Data
public class ReservationApproveDTO {
    private String rejectReason; // 驳回理由，如果为空则表示通过
}

