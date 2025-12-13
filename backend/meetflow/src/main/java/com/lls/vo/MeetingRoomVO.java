package com.lls.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会议室视图对象
 */
@Data
public class MeetingRoomVO {
    private Long id;
    private String name;
    private String roomNumber;
    private Integer capacity;
    private BigDecimal area;
    private String purpose;
    private String photoUrl;
    private Integer status;
    private LocalDateTime createTime;
}

