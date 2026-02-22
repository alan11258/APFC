package com.tpt.apfc.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tpt.apfc.bean.LoginUser;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Profile("mock")
public class MockLoginUserService {

    private final Map<String, LoginUser> byAccount;

    private final MockLoginAttemptTracker attemptTracker;

    public MockLoginUserService(MockLoginAttemptTracker attemptTracker) {
        this.attemptTracker = attemptTracker;
        this.byAccount = loadFromJson();
    }

    public Optional<LoginUser> findByAccount(String account) {
        if (account == null) return Optional.empty();

        LoginUser base = byAccount.get(account);
        if (base == null) return Optional.empty();

        // 合成 in-memory 的 loginErrorCount / lock 狀態（不寫回 JSON）
        int failures = attemptTracker.currentFailureCount(account);
        LoginUser view = new LoginUser();
        view.setId(base.getId());
        view.setAccount(base.getAccount());
        view.setName(base.getName());
        view.setPasswordHash(base.getPasswordHash());
        view.setEmail(base.getEmail());
        view.setCreatedAt(base.getCreatedAt());
        view.setLastLoginAt(base.getLastLoginAt());
        view.setLoginErrorCount(failures);

        Integer status = base.getStatus();
        if (status != null && status == 1 && attemptTracker.shouldLock(failures)) {
            status = 2; // 鎖定（mock 模式只在記憶體呈現）
        }
        view.setStatus(status);

        return Optional.of(view);
    }

    public List<LoginUser> listAllMasked() {
        // password 已被 @JsonIgnore；這裡直接回即可
        return new ArrayList<>(byAccount.values());
    }

    private Map<String, LoginUser> loadFromJson() {
        try (InputStream in = new ClassPathResource("mock/loginUser.json").getInputStream()) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            List<LoginUser> list = mapper.readValue(in, new TypeReference<List<LoginUser>>() {});
            return list.stream().collect(Collectors.toMap(
                    LoginUser::getAccount,
                    Function.identity(),
                    (a, b) -> a,
                    LinkedHashMap::new
            ));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load mock data from classpath: mock/loginUser.json", e);
        }
    }
}