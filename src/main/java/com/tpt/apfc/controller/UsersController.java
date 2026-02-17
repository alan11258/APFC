package com.tpt.apfc.controller;

import com.tpt.apfc.bean.UsersEntity;
import com.tpt.apfc.service.UsersQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{userId}")
    public ResponseEntity<UsersEntity> getUserById(@PathVariable Integer userId) {

        System.out.println("api/users/userId=" + userId );

        UsersEntity user = usersService.findById(userId);
        return (user == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(user);
    }
}
