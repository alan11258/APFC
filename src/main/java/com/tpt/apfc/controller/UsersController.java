package com.tpt.apfc.controller;

import com.tpt.apfc.bean.UsersEntity;
import com.tpt.apfc.service.UsersQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UsersController {

    //private final UsersRepository usersRepository;

    private final UsersQueryService usersService;

    //public UsersController(UsersRepository usersRepository) {
    //    this.usersRepository = usersRepository;
    //}

    public UsersController(UsersQueryService usersService) {
        this.usersService = usersService;
    }

    /*
    @GetMapping("/{userId}")
    public ResponseEntity<Users> getUserById(@PathVariable Integer userId) {
        return usersRepository.findById(userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    */

    @GetMapping
    public ResponseEntity<Map<String, Object>> missingUserIdHint() {

        log.info("api/users/missingUserIdHint");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "請帶入 user_id");
        body.put("example", "/api/users/{userId} 例如 /api/users/123");

        return ResponseEntity.badRequest().body(body);
    }


    @GetMapping("/{userId}")
    public ResponseEntity<UsersEntity> getUserById(@PathVariable Integer userId) {

        log.info("api/users/userId={}", userId);

        UsersEntity user = usersService.findById(userId);
        return (user == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(user);
    }
}
