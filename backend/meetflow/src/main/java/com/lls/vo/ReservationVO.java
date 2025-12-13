package com.lls.vo;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预约记录视图对象
 */
@Data
public class ReservationVO {
    private Long id;
    private Long userId;
    private String userName;
    private Long meetingRoomId;
    private String meetingRoomName;
    private String roomNumber;
    private String meetingTitle;
    private LocalDate reservationDate;
    private Integer startTime;
    private Integer endTime;
    private Integer attendeeCount;
    private Integer status;
    private String rejectReason;
    private LocalDateTime approveTime;
    private Long approverId;
    private String approverName;
    private LocalDateTime createTime;
}

