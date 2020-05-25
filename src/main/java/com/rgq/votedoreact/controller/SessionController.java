package com.rgq.votedoreact.controller;

import com.rgq.votedoreact.dto.CreateSessionDTO;
import com.rgq.votedoreact.dto.JoinSessionDTO;
import com.rgq.votedoreact.dto.SessionDTO;
import com.rgq.votedoreact.model.Session;
import com.rgq.votedoreact.service.SessionService;
import com.rgq.votedoreact.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

    @GetMapping(value = "/subPublic", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<SessionDTO> subPublicSession() {
        return service.getSessionStream(true).map(session -> service.sessionDTOMapper(session));
    }

    @PostMapping("/leave/{id}")
    public void leaveSession(@PathVariable String id) {
        // wip: Get user with id and check if he's the owner
        System.out.println(">>> userId: " + id + " <<<");
    }

    @PostMapping("/join")
    public Mono<ResponseEntity<?>> joinSession(@RequestBody JoinSessionDTO joinSessionDTO) {
        return userService.getById(joinSessionDTO.getUserId())
            .flatMap(user -> {
                if(user.getSessionId() == null) {
                    user.setSessionId(joinSessionDTO.getSessionId());
                    return userService.save(user).map(savedUser -> service.getById(joinSessionDTO.getSessionId())
                        .map(session -> {
                            session.getMembers().add(savedUser);
                            // !!Can't update document in a capped collection!!
                            service.save(session).subscribe();
                            return service.sessionDTOMapper(session);
                        }));
                }
                // wip: User already in a session
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
                    return service.save(new Session(
                            null,
                            createSessionDTO.getName(),
                            true,
                            user,
                            new ArrayList<>()
                        )).map(session -> {
                            user.setSessionId(session.getId());
                            userService.save(user).subscribe();
                            return service.sessionDTOMapper(session);
                        });
                }
                // wip: User already in a session
                return Mono.just(user.getSessionId());
            }).map(response -> {
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
