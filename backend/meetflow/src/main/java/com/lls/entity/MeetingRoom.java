package com.lls.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会议室实体类
 */
@Data
public class MeetingRoom {
    /**
     * 会议室ID
     */
    private Long id;

    /**
     * 会议室名称
     */
    private String name;

    /**
     * 房号
     */
    private String roomNumber;

    /**
     * 容纳人数
     */
    private Integer capacity;

    /**
     * 面积（平方米）
     */
    private BigDecimal area;

    /**
     * 用途描述
     */
    private String purpose;

    /**
     * 照片URL（阿里云OSS）
     */
    private String photoUrl;

    /**
     * 状态：0-不可预约，1-可预约
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

