package com.lls.service;

import com.lls.dto.LoginDTO;
import com.lls.dto.RegisterDTO;
import com.lls.dto.UpdateUserDTO;
import com.lls.vo.LoginVO;
import com.lls.vo.UserVO;

/**
 * 认证服务接口
 */
public interface AuthService {
    /**
     * 用户注册
     */
    void register(RegisterDTO registerDTO);

    /**
     * 用户登录
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 获取当前用户信息
     */
    UserVO getCurrentUser(Long userId);

    /**
     * 更新用户信息
     */
    UserVO updateUser(Long userId, UpdateUserDTO updateUserDTO);
}

