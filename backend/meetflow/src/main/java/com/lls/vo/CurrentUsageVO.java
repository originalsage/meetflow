package com.lls.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 当前会议室使用状态视图对象
 */
@Data
public class CurrentUsageVO {
    /**
     * 是否有当前使用中的会议室
     */
    private Boolean hasCurrentUsage;
    
    /**
     * 预约信息
     */
    private ReservationVO reservation;
    
    /**
     * 状态：pending-待开始, ongoing-进行中, ended-已结束
     */
    private String status;
    
    /**
     * 会议开始时间
     */
    private LocalDateTime startDateTime;
    
    /**
     * 会议结束时间
     */
    private LocalDateTime endDateTime;
    
    /**
     * 距离开始/已进行/已结束的时间（秒）
     */
    private Long timeDiffSeconds;
    
    /**
     * 下一个待使用的会议室（如果没有当前使用）
     */
    private ReservationVO nextReservation;
}

