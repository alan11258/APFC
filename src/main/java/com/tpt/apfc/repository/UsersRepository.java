package com.tpt.apfc.repository;

import com.tpt.apfc.bean.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepository extends JpaRepository<UsersEntity, Integer> {
}
