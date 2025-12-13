package com.lls.service;

import com.lls.dto.LoginDTO;
import com.lls.vo.LoginVO;
import com.lls.vo.UserVO;

/**
 * 认证服务接口
 */
public interface AuthService {
    /**
     * 用户登录
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 获取当前用户信息
     */
    UserVO getCurrentUser(Long userId);
}

