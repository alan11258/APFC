package com.tpt.apfc.bean;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "login_users")
public class LoginUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 64)
    private String account;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(nullable = false, length = 200)
    private String passwordHash; // 目前依你的 mock 需求先用明文；正式環境建議改 BCrypt

    @Column(nullable = false, length = 120)
    private String email;

    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    @Column(nullable = false)
    private Integer loginErrorCount = 0;

    /**
     * 1:啟用,2:鎖定,3:刪除
     */
    @Column(nullable = false)
    private Integer status = 1;
}
