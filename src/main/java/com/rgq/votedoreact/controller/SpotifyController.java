package com.rgq.votedoreact.controller;

import com.rgq.votedoreact.dto.AccessDTO;
import com.rgq.votedoreact.model.User;
import com.rgq.votedoreact.service.SpotifyService;
import com.rgq.votedoreact.service.SseService;
import com.rgq.votedoreact.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/spotify")
public class SpotifyController {
    private SpotifyService service;
    private UserService userService;
    private SseService sseService;

    public SpotifyController(SpotifyService service, UserService userService, SseService sseService) {
        this.service = service;
        this.userService = userService;
        this.sseService = sseService;
    }

    @GetMapping("/auth")
    public ResponseEntity<String> getAuth() {
        return ResponseEntity.ok(service.getUrl());
    }

    @PostMapping("/token")
    public Mono<ResponseEntity<?>> setToken(@RequestBody AccessDTO accessDTO) {
        User user = service.getSpotifyUser(accessDTO);
        if(user == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid token"));
        }
        return sseService.existCollectionWithName(user.getId()).flatMap(exist -> {
            // Check if user already exists in db
            if(exist) {
                return userService.getById(user.getId()).flatMap(response -> {
                    // Keep existing fields 'sessionId', 'friends'
                    user.setSessionId(response.getSessionId());
                    user.setFriends(response.getFriends());
                    return userService.save(user);
                });
            } else {
                // Create new capped Collection for user
                sseService.createCollectionWithName(user.getId());
                return userService.save(user);
            }
        }).map(savedUser -> ResponseEntity.ok(userService.userDTOMapper(savedUser)));
    }
}
