package com.rgq.votedoreact.controller;

import com.rgq.votedoreact.dto.CreateSessionDTO;
import com.rgq.votedoreact.model.Session;
import com.rgq.votedoreact.service.SessionService;
import com.rgq.votedoreact.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/session")
public class SessionController {
    private SessionService service;
    private UserService userService;

    public SessionController(SessionService service, UserService userService) {
        this.service = service;
        this.userService = userService;
    }

    @PostMapping("/create")
    public Mono<ResponseEntity<?>> createSession(@RequestBody CreateSessionDTO createSessionDTO) {
        return userService.getById(createSessionDTO.getUserId())
                .map(user -> service.save(new Session(null, createSessionDTO.getName(), user)))
                .map(ResponseEntity::ok);
    }

    /* test sample */
    @GetMapping("/{id}")
    public Mono<Session> getSessionById(@PathVariable String id) {
        return service.getById(id);
    }
}
