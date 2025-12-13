package com.lls.controller;

import com.lls.common.JwtUtil;
import com.lls.common.Result;
import com.lls.dto.MeetingRoomDTO;
import com.lls.dto.ReservationQueryDTO;
import com.lls.service.MeetingRoomService;
import com.lls.vo.MeetingRoomVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

/**
 * 会议室控制器
 */
@RestController
@RequestMapping("/api/meeting-rooms")
@RequiredArgsConstructor
public class MeetingRoomController {

    private final MeetingRoomService meetingRoomService;
    private final JwtUtil jwtUtil;

    /**
     * 获取所有会议室列表
     */
    @GetMapping
    public Result<List<MeetingRoomVO>> listMeetingRooms() {
        List<MeetingRoomVO> meetingRooms = meetingRoomService.listMeetingRooms();
        return Result.success(meetingRooms);
    }

    /**
     * 获取会议室详情
     */
    @GetMapping("/{id}")
    public Result<MeetingRoomVO> getMeetingRoomById(@PathVariable Long id) {
        MeetingRoomVO meetingRoom = meetingRoomService.getMeetingRoomById(id);
        return Result.success(meetingRoom);
    }

    /**
     * 根据条件筛选可用会议室
     */
    @GetMapping("/available")
    public Result<List<MeetingRoomVO>> findAvailableRooms(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Integer startTime,
            @RequestParam(required = false) Integer endTime,
            @RequestParam(required = false) Integer attendeeCount) {
        ReservationQueryDTO queryDTO = new ReservationQueryDTO();
        queryDTO.setDate(date);
        queryDTO.setStartTime(startTime);
        queryDTO.setEndTime(endTime);
        queryDTO.setAttendeeCount(attendeeCount);
        
        List<MeetingRoomVO> meetingRooms = meetingRoomService.findAvailableRooms(queryDTO);
        return Result.success(meetingRooms);
    }

    /**
     * 添加会议室（管理员）
     */
    @PostMapping
    public Result<MeetingRoomVO> addMeetingRoom(@Valid @RequestBody MeetingRoomDTO meetingRoomDTO,
                                                 HttpServletRequest request) {
        checkAdmin(request);
        MeetingRoomVO meetingRoom = meetingRoomService.addMeetingRoom(meetingRoomDTO);
        return Result.success(meetingRoom);
    }

    /**
     * 修改会议室（管理员）
     */
    @PutMapping("/{id}")
    public Result<MeetingRoomVO> updateMeetingRoom(@PathVariable Long id,
                                                    @Valid @RequestBody MeetingRoomDTO meetingRoomDTO,
                                                    HttpServletRequest request) {
        checkAdmin(request);
        MeetingRoomVO meetingRoom = meetingRoomService.updateMeetingRoom(id, meetingRoomDTO);
        return Result.success(meetingRoom);
    }

    /**
     * 删除会议室（管理员）
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteMeetingRoom(@PathVariable Long id, HttpServletRequest request) {
        checkAdmin(request);
        meetingRoomService.deleteMeetingRoom(id);
        return Result.success();
    }

    /**
     * 设置会议室状态（管理员）
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateRoomStatus(@PathVariable Long id,
                                         @RequestParam Integer status,
                                         HttpServletRequest request) {
        checkAdmin(request);
        meetingRoomService.updateRoomStatus(id, status);
        return Result.success();
    }

    /**
     * 上传会议室照片
     */
    @PostMapping("/upload")
    public Result<String> uploadPhoto(@RequestParam("file") MultipartFile file,
                                      HttpServletRequest request) {
        checkAdmin(request);
        String photoUrl = meetingRoomService.uploadPhoto(file);
        return Result.success(photoUrl);
    }

    /**
     * 检查是否为管理员
     */
    private void checkAdmin(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Integer role = jwtUtil.getRoleFromToken(token);
        if (role == null || role != 1) {
            throw new RuntimeException("无权限访问");
        }
    }

    /**
     * 从请求中获取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7).trim();
            // 去除可能的引号
            if (token.startsWith("\"") && token.endsWith("\"")) {
                token = token.substring(1, token.length() - 1);
            }
            return token;
        }
        throw new RuntimeException("未找到Token");
    }
}

