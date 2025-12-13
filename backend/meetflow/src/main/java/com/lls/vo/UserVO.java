package com.lls.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户视图对象
 */
@Data
public class UserVO {
    private Long id;
    private String username;
    private String name;
    private Integer role;
    private String phone;
    private String email;
    private LocalDateTime createTime;
}

