package com.tpt.apfc.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tpt.apfc.bean.LoginUser;
import com.tpt.apfc.bean.LoginUserEntity;
import com.tpt.apfc.repository.LoginUserRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.InputStream;
import java.util.List;

@Configuration
public class LoginUserSeedConfig {

    @Bean
    public ApplicationRunner seedLoginUsers(LoginUserRepository repo, PasswordEncoder passwordEncoder) {
        return args -> {
            ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

            try (InputStream in = new ClassPathResource("mock/loginUser.json").getInputStream()) {
                List<LoginUser> list = mapper.readValue(in, new TypeReference<List<LoginUser>>() {});
                for (LoginUser u : list) {
                    boolean exists = repo.findByAccount(u.getAccount()).isPresent();
                    if (exists) continue;

                    LoginUserEntity e = new LoginUserEntity();
                    e.setAccount(u.getAccount());
                    e.setName(u.getName());

                    // JSON 應提供 passwordHash；若你暫時仍放明文，也會在這裡 encode（避免把明文存入 DB）
                    String ph = u.getPasswordHash();
                    if (ph == null || ph.isBlank()) {
                        throw new IllegalStateException("loginUser.json missing passwordHash for account=" + u.getAccount());
                    }
                    // 若已經是 bcrypt（通常以 $2a/$2b/$2y 開頭），就直接用；否則 encode 一次
                    if (ph.startsWith("$2a$") || ph.startsWith("$2b$") || ph.startsWith("$2y$")) {
                        e.setPasswordHash(ph);
                    } else {
                        e.setPasswordHash(passwordEncoder.encode(ph));
                    }

                    e.setEmail(u.getEmail());
                    e.setCreatedAt(u.getCreatedAt());
                    e.setLastLoginAt(u.getLastLoginAt());
                    e.setLoginErrorCount(u.getLoginErrorCount() == null ? 0 : u.getLoginErrorCount());
                    e.setStatus(u.getStatus() == null ? 1 : u.getStatus());

                    repo.save(e);
                }
            }
        };
    }
}