package com.rgq.votedoreact.controller;

import com.rgq.votedoreact.model.User;
import com.rgq.votedoreact.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
