package com.lls.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码生成工具类
 * 用于生成BCrypt加密的密码
 */
public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 生成admin123的BCrypt密码
        String adminPassword = encoder.encode("admin123");
        System.out.println("admin123 的BCrypt密码: " + adminPassword);
        
        // 生成user123的BCrypt密码
        String userPassword = encoder.encode("user123");
        System.out.println("user123 的BCrypt密码: " + userPassword);
        
        // 验证密码
        System.out.println("\n验证密码:");
        System.out.println("admin123 匹配: " + encoder.matches("admin123", adminPassword));
        System.out.println("user123 匹配: " + encoder.matches("user123", userPassword));
    }
}

