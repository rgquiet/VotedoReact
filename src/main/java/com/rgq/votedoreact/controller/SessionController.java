package com.rgq.votedoreact.controller;

import com.rgq.votedoreact.model.Session;
import com.rgq.votedoreact.service.SessionService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/session")
public class SessionController {
    private SessionService service;

    public SessionController(SessionService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public Mono<Session> getSessionById(@PathVariable String id) {
        return service.getById(id);
    }
}
