package com.tpt.apfc.service;

import com.tpt.apfc.bean.LoginUserEntity;
import com.tpt.apfc.repository.LoginUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class LoginUserDbService {

    public static final int LOCK_THRESHOLD = 5;

    private final LoginUserRepository repo;

    public LoginUserDbService(LoginUserRepository repo) {
        this.repo = repo;
    }

    public Optional<LoginUserEntity> findByAccount(String account) {
        return repo.findByAccount(account);
    }

    @Transactional
    public void recordLoginSuccess(String account) {
        repo.findByAccount(account).ifPresent(u -> {
            u.setLastLoginAt(LocalDateTime.now());
            u.setLoginErrorCount(0);
            repo.save(u);
        });
    }

    @Transactional
    public void recordLoginFailure(String account) {
        repo.findByAccount(account).ifPresent(u -> {
            int next = (u.getLoginErrorCount() == null ? 0 : u.getLoginErrorCount()) + 1;
            u.setLoginErrorCount(next);

            if (next >= LOCK_THRESHOLD) {
                u.setStatus(2); // 鎖定
            }
            repo.save(u);
        });
    }
}
