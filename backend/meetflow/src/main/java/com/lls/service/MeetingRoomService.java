package com.lls.service;

import com.lls.dto.MeetingRoomDTO;
import com.lls.dto.ReservationQueryDTO;
import com.lls.vo.MeetingRoomVO;

import java.util.List;

/**
 * 会议室服务接口
 */
public interface MeetingRoomService {
    /**
     * 查询所有会议室
     */
    List<MeetingRoomVO> listMeetingRooms();

    /**
     * 根据ID查询会议室
     */
    MeetingRoomVO getMeetingRoomById(Long id);

    /**
     * 根据条件筛选可用会议室
     */
    List<MeetingRoomVO> findAvailableRooms(ReservationQueryDTO queryDTO);

    /**
     * 添加会议室
     */
    MeetingRoomVO addMeetingRoom(MeetingRoomDTO meetingRoomDTO);

    /**
     * 更新会议室
     */
    MeetingRoomVO updateMeetingRoom(Long id, MeetingRoomDTO meetingRoomDTO);

    /**
     * 删除会议室
     */
    void deleteMeetingRoom(Long id);

    /**
     * 更新会议室状态
     */
    void updateRoomStatus(Long id, Integer status);

    /**
     * 上传会议室照片
     */
    String uploadPhoto(org.springframework.web.multipart.MultipartFile file);
}

