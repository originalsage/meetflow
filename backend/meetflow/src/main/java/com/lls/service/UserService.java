package com.lls.service;

import com.lls.dto.PromoteUserDTO;
import com.lls.vo.UserVO;

import java.util.List;

/**
 * 用户管理服务接口
 */
public interface UserService {
    /**
     * 获取所有用户列表
     */
    List<UserVO> getAllUsers();

    /**
     * 删除用户
     */
    void deleteUser(Long userId);

    /**
     * 提升用户权限（只能提升至普通管理员）
     */
    void promoteUser(PromoteUserDTO promoteUserDTO);

    /**
     * 降级用户权限（只能将管理员降为普通用户）
     */
    void demoteUser(Long userId);
}

