package com.lls.service.impl;

import com.lls.common.ResultCode;
import com.lls.dto.ReservationDTO;
import com.lls.entity.MeetingRoom;
import com.lls.entity.Reservation;
import com.lls.entity.User;
import com.lls.mapper.MeetingRoomMapper;
import com.lls.mapper.ReservationMapper;
import com.lls.mapper.UserMapper;
import com.lls.service.ReservationService;
import com.lls.vo.CurrentUsageVO;
import com.lls.vo.ReservationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 预约服务实现类
 */
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationMapper reservationMapper;
    private final MeetingRoomMapper meetingRoomMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public ReservationVO createReservation(Long userId, ReservationDTO reservationDTO) {
        // 验证时间参数
        if (reservationDTO.getStartTime() >= reservationDTO.getEndTime()) {
            throw new RuntimeException(ResultCode.RESERVATION_TIME_INVALID.getMessage());
        }

        // 验证日期不能是过去
        LocalDate today = LocalDate.now();
        if (reservationDTO.getReservationDate().isBefore(today)) {
            throw new RuntimeException("不能预约过去的日期");
        }
        
        // 如果是今天，检查时间段是否已过
        if (reservationDTO.getReservationDate().equals(today)) {
            int currentHour = LocalDateTime.now().getHour();
            if (reservationDTO.getStartTime() <= currentHour) {
                throw new RuntimeException("不能预约已过去的时间段");
            }
        }

        // 查询会议室
        MeetingRoom meetingRoom = meetingRoomMapper.selectById(reservationDTO.getMeetingRoomId());
        if (meetingRoom == null) {
            throw new RuntimeException(ResultCode.MEETING_ROOM_NOT_FOUND.getMessage());
        }

        // 检查会议室是否可预约
        if (meetingRoom.getStatus() == 0) {
            throw new RuntimeException(ResultCode.MEETING_ROOM_NOT_AVAILABLE.getMessage());
        }

        // 检查容量是否足够
        if (meetingRoom.getCapacity() < reservationDTO.getAttendeeCount()) {
            throw new RuntimeException(ResultCode.MEETING_ROOM_CAPACITY_INSUFFICIENT.getMessage());
        }

        // 检查时间冲突
        List<Reservation> conflicts = reservationMapper.selectByRoomAndTime(
                reservationDTO.getMeetingRoomId(),
                reservationDTO.getReservationDate(),
                reservationDTO.getStartTime(),
                reservationDTO.getEndTime(),
                null
        );
        if (!conflicts.isEmpty()) {
            throw new RuntimeException(ResultCode.RESERVATION_TIME_CONFLICT.getMessage());
        }

        // 创建预约记录
        Reservation reservation = new Reservation();
        BeanUtils.copyProperties(reservationDTO, reservation);
        reservation.setUserId(userId);
        reservation.setStatus(0); // 待审批

        reservationMapper.insert(reservation);
        return getReservationById(reservation.getId());
    }

    @Override
    public List<ReservationVO> getMyReservations(Long userId, Integer status) {
        List<Reservation> reservations = reservationMapper.selectByUserId(userId, status);
        return reservations.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public ReservationVO getReservationById(Long id) {
        Reservation reservation = reservationMapper.selectById(id);
        if (reservation == null) {
            throw new RuntimeException(ResultCode.RESERVATION_NOT_FOUND.getMessage());
        }
        return convertToVO(reservation);
    }

    @Override
    @Transactional
    public void cancelReservation(Long id, Long userId) {
        Reservation reservation = reservationMapper.selectById(id);
        if (reservation == null) {
            throw new RuntimeException(ResultCode.RESERVATION_NOT_FOUND.getMessage());
        }

        // 验证权限
        if (!reservation.getUserId().equals(userId)) {
            throw new RuntimeException("无权限取消此预约");
        }

        // 检查状态
        if (reservation.getStatus() == 3) {
            throw new RuntimeException(ResultCode.RESERVATION_ALREADY_CANCELLED.getMessage());
        }
        if (reservation.getStatus() == 1) {
            // 已通过的预约可以取消
        }

        // 更新状态为已取消
        reservation.setStatus(3);
        reservationMapper.update(reservation);
    }

    @Override
    public List<ReservationVO> getAllReservations(Integer status) {
        List<Reservation> reservations = reservationMapper.selectAll(status);
        return reservations.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void approveReservation(Long id, Long approverId) {
        Reservation reservation = reservationMapper.selectById(id);
        if (reservation == null) {
            throw new RuntimeException(ResultCode.RESERVATION_NOT_FOUND.getMessage());
        }

        if (reservation.getStatus() != 0) {
            throw new RuntimeException(ResultCode.RESERVATION_STATUS_INVALID.getMessage());
        }

        // 审批时再次验证会议室状态和容量
        MeetingRoom meetingRoom = meetingRoomMapper.selectById(reservation.getMeetingRoomId());
        if (meetingRoom == null) {
            throw new RuntimeException(ResultCode.MEETING_ROOM_NOT_FOUND.getMessage());
        }

        // 检查会议室是否可预约
        if (meetingRoom.getStatus() == 0) {
            throw new RuntimeException(ResultCode.MEETING_ROOM_NOT_AVAILABLE.getMessage());
        }

        // 检查容量是否足够
        if (meetingRoom.getCapacity() < reservation.getAttendeeCount()) {
            throw new RuntimeException(ResultCode.MEETING_ROOM_CAPACITY_INSUFFICIENT.getMessage());
        }

        // 审批时再次检查时间冲突（排除当前预约本身）
        List<Reservation> conflicts = reservationMapper.selectByRoomAndTime(
                reservation.getMeetingRoomId(),
                reservation.getReservationDate(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getId() // 排除当前预约
        );
        if (!conflicts.isEmpty()) {
            throw new RuntimeException(ResultCode.RESERVATION_TIME_CONFLICT.getMessage());
        }

        reservation.setStatus(1); // 已通过
        reservation.setApproveTime(LocalDateTime.now());
        reservation.setApproverId(approverId);
        reservationMapper.update(reservation);
    }

    @Override
    @Transactional
    public void rejectReservation(Long id, Long approverId, String rejectReason) {
        Reservation reservation = reservationMapper.selectById(id);
        if (reservation == null) {
            throw new RuntimeException(ResultCode.RESERVATION_NOT_FOUND.getMessage());
        }

        if (reservation.getStatus() != 0) {
            throw new RuntimeException(ResultCode.RESERVATION_STATUS_INVALID.getMessage());
        }

        reservation.setStatus(2); // 已驳回
        reservation.setRejectReason(rejectReason);
        reservation.setApproveTime(LocalDateTime.now());
        reservation.setApproverId(approverId);
        reservationMapper.update(reservation);
    }

    @Override
    public List<ReservationVO> getWeeklySchedule(LocalDate weekStartDate) {
        List<Reservation> reservations = reservationMapper.selectWeeklySchedule(weekStartDate);
        return reservations.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservationVO> getDailySchedule(LocalDate date) {
        List<Reservation> reservations = reservationMapper.selectDailySchedule(date);
        return reservations.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public CurrentUsageVO getCurrentUsage(Long userId) {
        CurrentUsageVO usageVO = new CurrentUsageVO();
        usageVO.setHasCurrentUsage(false);
        
        // 获取用户所有已通过的预约
        List<Reservation> reservations = reservationMapper.selectByUserId(userId, 1);
        LocalDateTime now = LocalDateTime.now();
        
        // 查找当前时间范围内的预约（开始前1小时到结束后1小时）
        for (Reservation reservation : reservations) {
            LocalDateTime startDateTime = reservation.getReservationDate()
                    .atTime(reservation.getStartTime(), 0);
            LocalDateTime endDateTime = reservation.getReservationDate()
                    .atTime(reservation.getEndTime(), 0);
            
            // 开始前1小时和结束后1小时的时间范围
            LocalDateTime rangeStart = startDateTime.minusHours(1);
            LocalDateTime rangeEnd = endDateTime.plusHours(1);
            
            // 检查当前时间是否在这个范围内（包含边界）
            if (!now.isBefore(rangeStart) && !now.isAfter(rangeEnd)) {
                usageVO.setHasCurrentUsage(true);
                ReservationVO reservationVO = convertToVO(reservation);
                usageVO.setReservation(reservationVO);
                usageVO.setStartDateTime(startDateTime);
                usageVO.setEndDateTime(endDateTime);
                
                // 判断状态
                if (now.isBefore(startDateTime)) {
                    // 待开始
                    usageVO.setStatus("pending");
                    usageVO.setTimeDiffSeconds(java.time.Duration.between(now, startDateTime).getSeconds());
                } else if (now.isBefore(endDateTime)) {
                    // 进行中
                    usageVO.setStatus("ongoing");
                    usageVO.setTimeDiffSeconds(java.time.Duration.between(startDateTime, now).getSeconds());
                } else {
                    // 已结束
                    usageVO.setStatus("ended");
                    usageVO.setTimeDiffSeconds(java.time.Duration.between(endDateTime, now).getSeconds());
                }
                
                return usageVO;
            }
        }
        
        // 如果没有当前使用中的，查找下一个待使用的预约
        Reservation nextReservation = reservations.stream()
                .filter(r -> {
                    LocalDateTime startDateTime = r.getReservationDate()
                            .atTime(r.getStartTime(), 0);
                    return startDateTime.isAfter(now);
                })
                .sorted((r1, r2) -> {
                    LocalDateTime start1 = r1.getReservationDate().atTime(r1.getStartTime(), 0);
                    LocalDateTime start2 = r2.getReservationDate().atTime(r2.getStartTime(), 0);
                    return start1.compareTo(start2);
                })
                .findFirst()
                .orElse(null);
        
        if (nextReservation != null) {
            usageVO.setNextReservation(convertToVO(nextReservation));
        }
        
        return usageVO;
    }

    /**
     * 转换为VO
     */
    private ReservationVO convertToVO(Reservation reservation) {
        ReservationVO vo = new ReservationVO();
        BeanUtils.copyProperties(reservation, vo);

        // 查询用户信息
        User user = userMapper.selectById(reservation.getUserId());
        if (user != null) {
            vo.setUserName(user.getName());
        }

        // 查询会议室信息
        MeetingRoom meetingRoom = meetingRoomMapper.selectById(reservation.getMeetingRoomId());
        if (meetingRoom != null) {
            vo.setMeetingRoomName(meetingRoom.getName());
            vo.setRoomNumber(meetingRoom.getRoomNumber());
            vo.setPhotoUrl(meetingRoom.getPhotoUrl());
            // 调试日志
            System.out.println("会议室ID: " + meetingRoom.getId() + ", 图片URL: " + meetingRoom.getPhotoUrl());
        } else {
            // 如果会议室已被删除，显示提示信息
            vo.setMeetingRoomName("会议室已删除");
            vo.setRoomNumber("-");
        }

        // 查询审批人信息
        if (reservation.getApproverId() != null) {
            User approver = userMapper.selectById(reservation.getApproverId());
            if (approver != null) {
                vo.setApproverName(approver.getName());
            }
        }

        return vo;
    }
}

