package com.rgq.votedoreact.controller;

import com.rgq.votedoreact.model.User;
import com.rgq.votedoreact.service.UserService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public Mono<User> getUserById(@PathVariable String id) {
        return service.getById(id);
    }

    @GetMapping("/testUser")
    public User getTestUser() {
        return service.getTest();
    }
}
