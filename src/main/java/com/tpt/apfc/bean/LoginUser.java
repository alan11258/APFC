package com.tpt.apfc.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LoginUser {
    private Integer id;
    private String account;
    private String name;

    @JsonIgnore // 避免 controller 回傳時把 hash 送出去
    private String passwordHash;

    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private Integer loginErrorCount;
    private Integer status; // 1:啟用,2:鎖定,3:刪除
}