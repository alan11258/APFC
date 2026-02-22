package com.tpt.apfc.repository;

import com.tpt.apfc.bean.LoginUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoginUserRepository extends JpaRepository<LoginUserEntity, Integer> {
    Optional<LoginUserEntity> findByAccount(String account);
}
