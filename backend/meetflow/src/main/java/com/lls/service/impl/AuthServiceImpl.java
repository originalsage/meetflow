package com.lls.service.impl;

import com.lls.common.JwtUtil;
import com.lls.common.ResultCode;
import com.lls.dto.LoginDTO;
import com.lls.entity.User;
import com.lls.mapper.UserMapper;
import com.lls.service.AuthService;
import com.lls.vo.LoginVO;
import com.lls.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证服务实现类
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 查询用户
        User user = userMapper.selectByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new RuntimeException(ResultCode.USERNAME_OR_PASSWORD_ERROR.getMessage());
        }

        // 验证密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException(ResultCode.USERNAME_OR_PASSWORD_ERROR.getMessage());
        }

        // 验证角色（如果指定了角色）
        if (loginDTO.getRole() != null && !user.getRole().equals(loginDTO.getRole())) {
            throw new RuntimeException("角色不匹配");
        }

        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        // 构建响应
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);

        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setName(user.getName());
        userVO.setRole(user.getRole());
        userVO.setPhone(user.getPhone());
        userVO.setEmail(user.getEmail());
        userVO.setCreateTime(user.getCreateTime());

        loginVO.setUserInfo(userVO);

        return loginVO;
    }

    @Override
    public UserVO getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException(ResultCode.USER_NOT_FOUND.getMessage());
        }

        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setName(user.getName());
        userVO.setRole(user.getRole());
        userVO.setPhone(user.getPhone());
        userVO.setEmail(user.getEmail());
        userVO.setCreateTime(user.getCreateTime());

        return userVO;
    }
}

