package com.tpt.apfc.service;

import com.tpt.apfc.bean.UsersEntity;

public interface UsersQueryService {
    UsersEntity findById(Integer userId);
}
