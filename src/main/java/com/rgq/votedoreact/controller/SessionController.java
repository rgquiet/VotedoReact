package com.rgq.votedoreact.controller;

import com.rgq.votedoreact.dto.CreateSessionDTO;
import com.rgq.votedoreact.dto.SessionDTO;
import com.rgq.votedoreact.dto.UserDTO;
import com.rgq.votedoreact.model.Session;
import com.rgq.votedoreact.service.SessionService;
import com.rgq.votedoreact.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/session")
public class SessionController {
    private SessionService service;
    private UserService userService;

    public SessionController(SessionService service, UserService userService) {
        this.service = service;
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<SessionDTO>> getSessionById(@PathVariable String id) {
        return service.getById(id)
            .map(session -> ResponseEntity.ok(service.sessionDTOMapper(session)))
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/findPublic/{name}")
    public Flux<SessionDTO> findPublicSession(@PathVariable String name) {
        if(name.equals("_")) {
            name = "";
        }
        return service.getOpenByNameLike(name).map(session -> service.sessionDTOMapper(session));
    }

    @PostMapping("/leave/{id}")
    public void leaveSession(@PathVariable String id) {
        // wip: Get user with id and check if he's the owner
        System.out.println(">>> userId: " + id + " <<<");
    }

    @PostMapping("/join")
    public Mono<ResponseEntity<?>> joinSession(@RequestBody UserDTO userDTO) {
        return userService.getById(userDTO.getId())
            .flatMap(user -> {
                if(user.getSessionId() == null) {
                    user.setSessionId(userDTO.getSessionId());
                    return userService.save(user)
                        .flatMap(savedUser -> service.getById(savedUser.getSessionId()))
                        .map(session -> {
                            session.getMembers().add(user);
                            service.save(session).subscribe();
                            return service.sessionDTOMapper(session);
                        });
                }
                // User already in a session
                return Mono.just(user.getSessionId());
            }).map(response -> {
                if(response instanceof SessionDTO) {
                    return ResponseEntity.ok(response);
                }
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            });
    }

    @PostMapping("/create")
    public Mono<ResponseEntity<?>> createSession(@RequestBody CreateSessionDTO createSessionDTO) {
        return userService.getById(createSessionDTO.getUserId())
            .flatMap(user -> {
                if(user.getSessionId() == null) {
                    Boolean open = false;
                    if(createSessionDTO.getInvitations() == null) {
                        open = true;
                    }
                    return service.save(new Session(
                        null,
                        createSessionDTO.getName(),
                        open,
                        user,
                        new ArrayList<>()
                    )).map(session -> {
                        user.setSessionId(session.getId());
                        userService.save(user).subscribe();
                        for(int i = 0; i < createSessionDTO.getInvitations().length; i++) {
                            service.sendInvitation(
                                session.getId(),
                                session.getName(),
                                user.getUsername(),
                                createSessionDTO.getInvitations()[i]
                            );
                        }
                        return service.sessionDTOMapper(session);
                    });
                }
                // User already in a session
                return Mono.just(user.getSessionId());
            }).map(response -> {
                if(response instanceof SessionDTO) {
                    return ResponseEntity.ok(response);
                }
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            });
    }
}
