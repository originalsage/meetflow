package com.lls.controller;

import com.lls.common.JwtUtil;
import com.lls.common.Result;
import com.lls.dto.ReservationApproveDTO;
import com.lls.dto.ReservationDTO;
import com.lls.service.ReservationService;
import com.lls.vo.ReservationVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 预约控制器
 */
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final JwtUtil jwtUtil;

    /**
     * 创建预约
     */
    @PostMapping
    public Result<ReservationVO> createReservation(@Valid @RequestBody ReservationDTO reservationDTO,
                                                    HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        ReservationVO reservation = reservationService.createReservation(userId, reservationDTO);
        return Result.success(reservation);
    }

    /**
     * 查询我的预约记录
     */
    @GetMapping("/my")
    public Result<List<ReservationVO>> getMyReservations(@RequestParam(required = false) Integer status,
                                                         HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        List<ReservationVO> reservations = reservationService.getMyReservations(userId, status);
        return Result.success(reservations);
    }

    /**
     * 获取预约详情
     */
    @GetMapping("/{id}")
    public Result<ReservationVO> getReservationById(@PathVariable Long id) {
        ReservationVO reservation = reservationService.getReservationById(id);
        return Result.success(reservation);
    }

    /**
     * 取消预约
     */
    @PutMapping("/{id}/cancel")
    public Result<Void> cancelReservation(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        reservationService.cancelReservation(id, userId);
        return Result.success();
    }

    /**
     * 查询所有预约记录（管理员）
     */
    @GetMapping("/all")
    public Result<List<ReservationVO>> getAllReservations(@RequestParam(required = false) Integer status,
                                                           HttpServletRequest request) {
        checkAdmin(request);
        List<ReservationVO> reservations = reservationService.getAllReservations(status);
        return Result.success(reservations);
    }

    /**
     * 审批预约（管理员）
     */
    @PutMapping("/{id}/approve")
    public Result<Void> approveReservation(@PathVariable Long id,
                                          @RequestBody(required = false) ReservationApproveDTO approveDTO,
                                          HttpServletRequest request) {
        checkAdmin(request);
        Long approverId = getUserIdFromRequest(request);

        if (approveDTO != null && approveDTO.getRejectReason() != null
                && !approveDTO.getRejectReason().trim().isEmpty()) {
            // 驳回
            reservationService.rejectReservation(id, approverId, approveDTO.getRejectReason());
        } else {
            // 通过
            reservationService.approveReservation(id, approverId);
        }
        return Result.success();
    }

    /**
     * 获取一周预约情况（管理员）
     */
    @GetMapping("/weekly")
    public Result<List<ReservationVO>> getWeeklySchedule(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate,
            HttpServletRequest request) {
        checkAdmin(request);
        List<ReservationVO> reservations = reservationService.getWeeklySchedule(weekStartDate);
        return Result.success(reservations);
    }

    /**
     * 获取当天各时段占用情况（管理员）
     */
    @GetMapping("/daily")
    public Result<List<ReservationVO>> getDailySchedule(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            HttpServletRequest request) {
        checkAdmin(request);
        List<ReservationVO> reservations = reservationService.getDailySchedule(date);
        return Result.success(reservations);
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
     * 从请求中获取用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        return jwtUtil.getUserIdFromToken(token);
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

