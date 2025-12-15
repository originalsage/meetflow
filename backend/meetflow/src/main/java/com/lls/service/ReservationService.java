package com.lls.service;

import com.lls.dto.ReservationApproveDTO;
import com.lls.dto.ReservationDTO;
import com.lls.vo.ReservationVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 预约服务接口
 */
public interface ReservationService {
    /**
     * 创建预约
     */
    ReservationVO createReservation(Long userId, ReservationDTO reservationDTO);

    /**
     * 查询我的预约记录
     */
    List<ReservationVO> getMyReservations(Long userId, Integer status);

    /**
     * 分页查询我的预约记录
     */
    com.lls.common.PageResult<ReservationVO> getMyReservationsPage(Long userId, Integer status, Integer page, Integer pageSize);

    /**
     * 查询预约详情
     */
    ReservationVO getReservationById(Long id);

    /**
     * 取消预约
     */
    void cancelReservation(Long id, Long userId);

    /**
     * 查询所有预约记录（管理员）
     */
    List<ReservationVO> getAllReservations(Integer status);

    /**
     * 分页查询所有预约记录（管理员）
     */
    com.lls.common.PageResult<ReservationVO> getAllReservationsPage(Integer status, Integer page, Integer pageSize);

    /**
     * 审批预约（通过）
     */
    void approveReservation(Long id, Long approverId);

    /**
     * 审批预约（驳回）
     */
    void rejectReservation(Long id, Long approverId, String rejectReason);

    /**
     * 获取一周预约情况
     */
    List<ReservationVO> getWeeklySchedule(LocalDate weekStartDate);

    /**
     * 获取当天各时段占用情况
     */
    List<ReservationVO> getDailySchedule(LocalDate date);

    /**
     * 获取用户当前会议室使用状态
     */
    com.lls.vo.CurrentUsageVO getCurrentUsage(Long userId);

    /**
     * 完成预约（确认使用）
     */
    void completeReservation(Long id, Long userId);
}

