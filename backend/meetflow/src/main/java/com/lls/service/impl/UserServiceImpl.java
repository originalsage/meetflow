package com.lls.service.impl;

import com.lls.common.ResultCode;
import com.lls.dto.PromoteUserDTO;
import com.lls.entity.User;
import com.lls.mapper.ReservationMapper;
import com.lls.mapper.UserMapper;
import com.lls.service.UserService;
import com.lls.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理服务实现类
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final ReservationMapper reservationMapper;

    @Override
    public List<UserVO> getAllUsers() {
        List<User> users = userMapper.selectAll();
        // 排序：超级管理员（role=2）排在第一位，其余按用户名字典序排序
        users.sort((u1, u2) -> {
            // 超级管理员排在第一位
            if (u1.getRole() == 2 && u2.getRole() != 2) {
                return -1;
            }
            if (u1.getRole() != 2 && u2.getRole() == 2) {
                return 1;
            }
            // 其余按用户名字典序排序
            return u1.getUsername().compareTo(u2.getUsername());
        });
        return users.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteUser(Long userId, Boolean deleteReservations) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException(ResultCode.USER_NOT_FOUND.getMessage());
        }
        
        // 不能删除超级管理员
        if (user.getRole() == 2) {
            throw new RuntimeException("不能删除超级管理员");
        }
        
        // 检查是否有预约记录
        Long reservationCount = reservationMapper.countByUserId(userId, null);
        if (reservationCount != null && reservationCount > 0) {
            if (deleteReservations == null || !deleteReservations) {
                throw new RuntimeException("该用户存在预约记录，无法删除。如需删除，请选择一并删除预约记录。");
            }
            // 删除该用户的所有预约记录
            List<com.lls.entity.Reservation> reservations = reservationMapper.selectByUserId(userId, null);
            for (com.lls.entity.Reservation reservation : reservations) {
                reservationMapper.deleteById(reservation.getId());
            }
        }
        
        userMapper.deleteById(userId);
    }

    @Override
    public void promoteUser(PromoteUserDTO promoteUserDTO) {
        User user = userMapper.selectById(promoteUserDTO.getUserId());
        if (user == null) {
            throw new RuntimeException(ResultCode.USER_NOT_FOUND.getMessage());
        }
        
        // 只能提升至普通管理员（role=1）
        if (promoteUserDTO.getRole() != 1) {
            throw new RuntimeException("只能将用户提升为普通管理员（role=1）");
        }
        
        // 不能提升超级管理员
        if (user.getRole() == 2) {
            throw new RuntimeException("超级管理员无需提升权限");
        }
        
        user.setRole(promoteUserDTO.getRole());
        user.setUpdateTime(LocalDateTime.now());
        int result = userMapper.update(user);
        if (result == 0) {
            throw new RuntimeException("更新用户失败");
        }
    }

    @Override
    public void demoteUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException(ResultCode.USER_NOT_FOUND.getMessage());
        }
        
        // 不能降级超级管理员
        if (user.getRole() == 2) {
            throw new RuntimeException("不能降级超级管理员");
        }
        
        // 只能降级管理员（role=1）为普通用户（role=0）
        if (user.getRole() != 1) {
            throw new RuntimeException("只能将管理员降为普通用户");
        }
        
        user.setRole(0);
        user.setUpdateTime(LocalDateTime.now());
        int result = userMapper.update(user);
        if (result == 0) {
            throw new RuntimeException("更新用户失败");
        }
    }

    /**
     * 将User实体转换为UserVO
     */
    private UserVO convertToVO(User user) {
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

