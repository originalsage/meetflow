package com.lls.controller;

import com.lls.common.JwtUtil;
import com.lls.common.Result;
import com.lls.dto.DemoteUserDTO;
import com.lls.dto.PromoteUserDTO;
import com.lls.service.UserService;
import com.lls.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 获取所有用户列表（仅超级管理员可访问）
     */
    @GetMapping
    public Result<List<UserVO>> getAllUsers(HttpServletRequest request) {
        // 验证是否为超级管理员
        String token = getTokenFromRequest(request);
        Integer role = jwtUtil.getRoleFromToken(token);
        if (role == null || role != 2) {
            throw new RuntimeException("无权限访问");
        }
        
        List<UserVO> users = userService.getAllUsers();
        return Result.success(users);
    }

    /**
     * 删除用户（仅超级管理员可访问）
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(HttpServletRequest request, 
                                    @PathVariable Long id,
                                    @RequestParam(required = false, defaultValue = "false") Boolean deleteReservations) {
        // 验证是否为超级管理员
        String token = getTokenFromRequest(request);
        Integer role = jwtUtil.getRoleFromToken(token);
        if (role == null || role != 2) {
            throw new RuntimeException("无权限访问");
        }
        
        userService.deleteUser(id, deleteReservations);
        return Result.success(null);
    }

    /**
     * 提升用户权限（仅超级管理员可访问）
     */
    @PutMapping("/promote")
    public Result<Void> promoteUser(HttpServletRequest request, @Valid @RequestBody PromoteUserDTO promoteUserDTO) {
        // 验证是否为超级管理员
        String token = getTokenFromRequest(request);
        Integer role = jwtUtil.getRoleFromToken(token);
        if (role == null || role != 2) {
            throw new RuntimeException("无权限访问");
        }
        
        userService.promoteUser(promoteUserDTO);
        return Result.success(null);
    }

    /**
     * 降级用户权限（仅超级管理员可访问）
     */
    @PutMapping("/demote")
    public Result<Void> demoteUser(HttpServletRequest request, @Valid @RequestBody DemoteUserDTO demoteUserDTO) {
        // 验证是否为超级管理员
        String token = getTokenFromRequest(request);
        Integer role = jwtUtil.getRoleFromToken(token);
        if (role == null || role != 2) {
            throw new RuntimeException("无权限访问");
        }
        
        userService.demoteUser(demoteUserDTO.getUserId());
        return Result.success(null);
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

