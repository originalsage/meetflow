package com.lls.service.impl;

import com.lls.common.JwtUtil;
import com.lls.common.ResultCode;
import com.lls.dto.LoginDTO;
import com.lls.dto.RegisterDTO;
import com.lls.dto.UpdateUserDTO;
import com.lls.entity.User;
import com.lls.mapper.UserMapper;
import com.lls.service.AuthService;
import com.lls.vo.LoginVO;
import com.lls.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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
    public void register(RegisterDTO registerDTO) {
        // 检查用户名是否已存在
        User existingUser = userMapper.selectByUsername(registerDTO.getUsername());
        if (existingUser != null) {
            throw new RuntimeException(ResultCode.USERNAME_ALREADY_EXISTS.getMessage());
        }

        // 验证两次密码是否一致
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new RuntimeException("两次输入的密码不一致");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setName(registerDTO.getName());
        user.setPhone(registerDTO.getPhone());
        user.setEmail(registerDTO.getEmail());
        user.setRole(0); // 默认角色为普通用户
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        // 保存用户
        userMapper.insert(user);
    }

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

    @Override
    public UserVO updateUser(Long userId, UpdateUserDTO updateUserDTO) {
        // 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException(ResultCode.USER_NOT_FOUND.getMessage());
        }

        // 更新用户信息
        user.setName(updateUserDTO.getName());
        user.setPhone(updateUserDTO.getPhone());
        user.setEmail(updateUserDTO.getEmail());
        user.setUpdateTime(LocalDateTime.now());

        // 保存更新
        userMapper.update(user);

        // 构建并返回UserVO
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

