package com.lls.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 快速生成BCrypt密码
 */
public class GeneratePassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        System.out.println("=== BCrypt密码生成 ===");
        System.out.println();
        
        String adminPwd = encoder.encode("admin123");
        System.out.println("admin123 -> " + adminPwd);
        System.out.println();
        
        String userPwd = encoder.encode("user123");
        System.out.println("user123 -> " + userPwd);
        System.out.println();
        
        System.out.println("=== 验证 ===");
        System.out.println("admin123验证: " + encoder.matches("admin123", adminPwd));
        System.out.println("user123验证: " + encoder.matches("user123", userPwd));
    }
}

