package com.lls.mapper;

import com.lls.dto.ReservationQueryDTO;
import com.lls.entity.MeetingRoom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 会议室Mapper接口
 */
@Mapper
public interface MeetingRoomMapper {
    /**
     * 查询所有会议室
     */
    List<MeetingRoom> selectAll();

    /**
     * 根据ID查询会议室
     */
    MeetingRoom selectById(@Param("id") Long id);

    /**
     * 根据房号查询会议室
     */
    MeetingRoom selectByRoomNumber(@Param("roomNumber") String roomNumber);

    /**
     * 根据条件查询可用会议室
     */
    List<MeetingRoom> selectAvailable(ReservationQueryDTO queryDTO);

    /**
     * 插入会议室
     */
    int insert(MeetingRoom meetingRoom);

    /**
     * 更新会议室
     */
    int update(MeetingRoom meetingRoom);

    /**
     * 根据ID删除会议室
     */
    int deleteById(@Param("id") Long id);
}

