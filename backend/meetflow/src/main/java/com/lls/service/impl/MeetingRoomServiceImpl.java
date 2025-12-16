package com.lls.service.impl;

import com.lls.common.ResultCode;
import com.lls.dto.MeetingRoomDTO;
import com.lls.dto.ReservationQueryDTO;
import com.lls.entity.MeetingRoom;
import com.lls.mapper.MeetingRoomMapper;
import com.lls.mapper.ReservationMapper;
import com.lls.service.MeetingRoomService;
import com.lls.service.OssService;
import com.lls.vo.MeetingRoomVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 会议室服务实现类
 */
@Service
@RequiredArgsConstructor
public class MeetingRoomServiceImpl implements MeetingRoomService {

    private final MeetingRoomMapper meetingRoomMapper;
    private final ReservationMapper reservationMapper;
    private final OssService ossService;

    @Override
    public List<MeetingRoomVO> listMeetingRooms() {
        List<MeetingRoom> meetingRooms = meetingRoomMapper.selectAll();
        return meetingRooms.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public MeetingRoomVO getMeetingRoomById(Long id) {
        MeetingRoom meetingRoom = meetingRoomMapper.selectById(id);
        if (meetingRoom == null) {
            throw new RuntimeException(ResultCode.MEETING_ROOM_NOT_FOUND.getMessage());
        }
        return convertToVO(meetingRoom);
    }

    @Override
    public List<MeetingRoomVO> findAvailableRooms(ReservationQueryDTO queryDTO) {
        // 验证时间参数
        if (queryDTO.getStartTime() != null && queryDTO.getEndTime() != null) {
            if (queryDTO.getStartTime() >= queryDTO.getEndTime()) {
                throw new RuntimeException("开始时间必须小于结束时间");
            }
        }

        List<MeetingRoom> meetingRooms = meetingRoomMapper.selectAvailable(queryDTO);
        return meetingRooms.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MeetingRoomVO addMeetingRoom(MeetingRoomDTO meetingRoomDTO) {
        // 检查房号是否已存在
        MeetingRoom existingRoom = meetingRoomMapper.selectByRoomNumber(meetingRoomDTO.getRoomNumber());
        if (existingRoom != null) {
            throw new RuntimeException(ResultCode.MEETING_ROOM_NUMBER_EXISTS.getMessage());
        }

        MeetingRoom meetingRoom = new MeetingRoom();
        BeanUtils.copyProperties(meetingRoomDTO, meetingRoom);
        if (meetingRoom.getStatus() == null) {
            meetingRoom.setStatus(1); // 默认可预约
        }

        meetingRoomMapper.insert(meetingRoom);
        return convertToVO(meetingRoom);
    }

    @Override
    @Transactional
    public MeetingRoomVO updateMeetingRoom(Long id, MeetingRoomDTO meetingRoomDTO) {
        MeetingRoom existingRoom = meetingRoomMapper.selectById(id);
        if (existingRoom == null) {
            throw new RuntimeException(ResultCode.MEETING_ROOM_NOT_FOUND.getMessage());
        }

        // 如果修改了房号，检查新房号是否已存在
        if (meetingRoomDTO.getRoomNumber() != null
                && !meetingRoomDTO.getRoomNumber().equals(existingRoom.getRoomNumber())) {
            MeetingRoom roomWithSameNumber = meetingRoomMapper.selectByRoomNumber(meetingRoomDTO.getRoomNumber());
            if (roomWithSameNumber != null) {
                throw new RuntimeException(ResultCode.MEETING_ROOM_NUMBER_EXISTS.getMessage());
            }
        }

        MeetingRoom meetingRoom = new MeetingRoom();
        meetingRoom.setId(id);
        
        // 手动设置属性，确保所有字段都能更新
        if (meetingRoomDTO.getName() != null) {
            meetingRoom.setName(meetingRoomDTO.getName());
        }
        if (meetingRoomDTO.getRoomNumber() != null) {
            meetingRoom.setRoomNumber(meetingRoomDTO.getRoomNumber());
        }
        if (meetingRoomDTO.getCapacity() != null) {
            meetingRoom.setCapacity(meetingRoomDTO.getCapacity());
        }
        if (meetingRoomDTO.getArea() != null) {
            meetingRoom.setArea(meetingRoomDTO.getArea());
        }
        if (meetingRoomDTO.getPurpose() != null) {
            meetingRoom.setPurpose(meetingRoomDTO.getPurpose());
        }
        if (meetingRoomDTO.getPhotoUrl() != null) {
            meetingRoom.setPhotoUrl(meetingRoomDTO.getPhotoUrl());
        }
        if (meetingRoomDTO.getStatus() != null) {
            meetingRoom.setStatus(meetingRoomDTO.getStatus());
        }

        meetingRoomMapper.update(meetingRoom);
        return getMeetingRoomById(id);
    }

    @Override
    @Transactional
    public void deleteMeetingRoom(Long id) {
        MeetingRoom meetingRoom = meetingRoomMapper.selectById(id);
        if (meetingRoom == null) {
            throw new RuntimeException(ResultCode.MEETING_ROOM_NOT_FOUND.getMessage());
        }
        
        // 检查是否存在相关预约记录
        List<com.lls.entity.Reservation> reservations = reservationMapper.selectByMeetingRoomId(id);
        if (reservations != null && !reservations.isEmpty()) {
            throw new RuntimeException(ResultCode.MEETING_ROOM_HAS_RESERVATIONS.getMessage());
        }
        
        meetingRoomMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void updateRoomStatus(Long id, Integer status) {
        MeetingRoom meetingRoom = meetingRoomMapper.selectById(id);
        if (meetingRoom == null) {
            throw new RuntimeException(ResultCode.MEETING_ROOM_NOT_FOUND.getMessage());
        }
        meetingRoom.setStatus(status);
        meetingRoomMapper.update(meetingRoom);
    }

    @Override
    public String uploadPhoto(MultipartFile file) {
        return ossService.uploadFile(file);
    }

    /**
     * 转换为VO
     */
    private MeetingRoomVO convertToVO(MeetingRoom meetingRoom) {
        MeetingRoomVO vo = new MeetingRoomVO();
        BeanUtils.copyProperties(meetingRoom, vo);
        return vo;
    }
}

