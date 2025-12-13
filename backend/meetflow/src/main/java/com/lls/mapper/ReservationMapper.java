package com.lls.mapper;

import com.lls.entity.Reservation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 预约记录Mapper接口
 */
@Mapper
public interface ReservationMapper {
    /**
     * 插入预约记录
     */
    int insert(Reservation reservation);

    /**
     * 根据ID查询预约记录
     */
    Reservation selectById(@Param("id") Long id);

    /**
     * 根据用户ID查询预约记录
     */
    List<Reservation> selectByUserId(@Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 查询所有预约记录
     */
    List<Reservation> selectAll(@Param("status") Integer status);

    /**
     * 查询时间冲突的预约记录
     */
    List<Reservation> selectByRoomAndTime(@Param("roomId") Long roomId,
                                          @Param("date") LocalDate date,
                                          @Param("startTime") Integer startTime,
                                          @Param("endTime") Integer endTime,
                                          @Param("excludeId") Long excludeId);

    /**
     * 更新预约记录
     */
    int update(Reservation reservation);

    /**
     * 查询一周的预约情况
     */
    List<Reservation> selectWeeklySchedule(@Param("weekStartDate") LocalDate weekStartDate);

    /**
     * 查询当天的预约情况
     */
    List<Reservation> selectDailySchedule(@Param("date") LocalDate date);
}

