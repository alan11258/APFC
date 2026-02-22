package com.tpt.apfc.controller;

import com.tpt.apfc.bean.LoginUser;
import com.tpt.apfc.service.MockLoginUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/login-users")
public class LoginUserController {

    private final MockLoginUserService loginUserService;

    public LoginUserController(MockLoginUserService loginUserService) {
        this.loginUserService = loginUserService;
    }

    @GetMapping
    public ResponseEntity<List<LoginUser>> list(Authentication auth) {
        // 建議：此 API 只有管理者可看；先簡化：登入後才可看
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(loginUserService.listAllMasked());
    }
}