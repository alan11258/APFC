package com.tpt.apfc.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tpt.apfc.bean.UsersEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Profile("mock")
public class MockUsersService implements UsersQueryService {

    private final Map<Integer, UsersEntity> demoData;

    public MockUsersService() {
        this.demoData = loadFromJson();
    }

    @Override
    public UsersEntity findById(Integer userId) {
        return demoData.get(userId);
    }

    private Map<Integer, UsersEntity> loadFromJson() {
        try (InputStream in = new ClassPathResource("mock/users.json").getInputStream()) {

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule()); // 支援 LocalDateTime（即使你 JSON 目前沒放 created_at）

            List<UsersEntity> list = mapper.readValue(in, new TypeReference<List<UsersEntity>>() {});
            return list.stream().collect(Collectors.toMap(
                    UsersEntity::getUser_id,
                    Function.identity()
            ));

        } catch (Exception e) {
            throw new IllegalStateException("Failed to load mock data from classpath: mock/users.json", e);
        }
    }
}