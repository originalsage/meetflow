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
        if (reservationDTO.getReservationDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("不能预约过去的日期");
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

