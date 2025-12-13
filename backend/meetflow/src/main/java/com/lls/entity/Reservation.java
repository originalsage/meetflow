package com.lls.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预约记录实体类
 */
@Data
public class Reservation {
    /**
     * 预约ID
     */
    private Long id;

    /**
     * 预约用户ID
     */
    private Long userId;

    /**
     * 会议室ID
     */
    private Long meetingRoomId;

    /**
     * 会议主题
     */
    private String meetingTitle;

    /**
     * 预约日期
     */
    private LocalDate reservationDate;

    /**
     * 开始时间（小时，0-23）
     */
    private Integer startTime;

    /**
     * 结束时间（小时，0-23）
     */
    private Integer endTime;

    /**
     * 参会人数
     */
    private Integer attendeeCount;

    /**
     * 状态：0-待审批，1-已通过，2-已驳回，3-已取消，4-已完成
     */
    private Integer status;

    /**
     * 驳回理由
     */
    private String rejectReason;

    /**
     * 审批时间
     */
    private LocalDateTime approveTime;

    /**
     * 审批人ID
     */
    private Long approverId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

