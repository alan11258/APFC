package com.tpt.apfc.service;

import com.tpt.apfc.bean.UsersEntity;
import com.tpt.apfc.repository.UsersRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@Profile("!mock")
public class UsersService implements UsersQueryService {

    public static final String USERS_BY_ID_CACHE = "usersById";

    private final UsersRepository usersRepository;

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String USERS_CACHE_KEY_PREFIX = "user:info:";

    private static final String USERS_VISIT_PREFIX = "user:visit:";

    private static final String LATEST_USERS_LIST = "user:latest:";

    public UsersService(UsersRepository usersRepository, RedisTemplate<String, Object> redisTemplate) {
        this.usersRepository = usersRepository;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public UsersEntity findById(Integer userId) {
        return findByIdCached(userId);
    }

    public UsersEntity findByIdCached(Integer userId) {

        String key = USERS_CACHE_KEY_PREFIX + userId;

        // 1. 查詢 Redis
        UsersEntity cachedUser = (UsersEntity) redisTemplate.opsForValue().get(key);

        if (cachedUser != null) {
            log.info("從 Redis 取得使用者資料: {}", userId);
            return cachedUser;
        }


        // 2. 查詢 DB (Cache Miss)
        UsersEntity dbUser = usersRepository.findById(userId).orElse(null);

        if ( dbUser != null ) {
            redisTemplate.opsForValue().set(key, dbUser, Duration.ofMinutes(30));
            log.info("從 DB 取得使用者資料, 並寫入 Redis: {}", userId);
        }
        return dbUser;
    }

    @Cacheable(cacheNames = USERS_BY_ID_CACHE, key = "#userId", unless = "#result == null")
    public UsersEntity findByIdRedisCached(Integer userId) {
        Optional<UsersEntity> userOpt = usersRepository.findById(userId);
        return userOpt.orElse(null);
    }

    @CachePut(cacheNames = USERS_BY_ID_CACHE, key = "#result.user_id", unless = "#result == null")
    public UsersEntity saveAndRefreshRedisCache(UsersEntity user) {
        return usersRepository.save(user);
    }

    @CacheEvict(cacheNames = USERS_BY_ID_CACHE, key = "#userId")
    public void deleteByIdAndEvictRedisCache(Integer userId) {
        usersRepository.deleteById(userId);
    }
}
