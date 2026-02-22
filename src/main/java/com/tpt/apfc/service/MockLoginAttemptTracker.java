package com.tpt.apfc.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Profile("mock")
public class MockLoginAttemptTracker {

    public static final int LOCK_THRESHOLD = 5;

    private final ConcurrentHashMap<String, AtomicInteger> failures = new ConcurrentHashMap<>();

    public int recordFailure(String account) {
        return failures.computeIfAbsent(account, k -> new AtomicInteger(0)).incrementAndGet();
    }

    public void recordSuccess(String account) {
        failures.remove(account);
    }

    public int currentFailureCount(String account) {
        AtomicInteger v = failures.get(account);
        return v == null ? 0 : v.get();
    }

    public boolean shouldLock(int failureCount) {
        return failureCount >= LOCK_THRESHOLD;
    }
}
