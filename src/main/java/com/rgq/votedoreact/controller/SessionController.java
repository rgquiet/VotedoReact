package com.rgq.votedoreact.controller;

import com.rgq.votedoreact.dto.CreateSessionDTO;
import com.rgq.votedoreact.dto.SessionDTO;
import com.rgq.votedoreact.model.Session;
import com.rgq.votedoreact.service.SessionService;
import com.rgq.votedoreact.service.UserService;
import org.springframework.http.HttpStatus;
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
            .flatMap(user -> {
                if(user.getSessionId() == null) {
                    return service.save(new Session(null, createSessionDTO.getName(), user))
                        .map(session -> {
                            user.setSessionId(session.getId());
                            userService.save(user).subscribe();
                            return service.sessionDTOMapper(session);
                        });
                }
                // User already in a session
                return Mono.just(user.getSessionId());
            })
            .map(response -> {
                if(response instanceof SessionDTO) {
                    return ResponseEntity.ok(response);
                }
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            });
    }

    /* test sample */
    @GetMapping("/{id}")
    public Mono<Session> getSessionById(@PathVariable String id) {
        return service.getById(id);
    }
}
