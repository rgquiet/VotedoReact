package com.rgq.votedoreact.controller;

import com.rgq.votedoreact.dto.AccessDTO;
import com.rgq.votedoreact.model.User;
import com.rgq.votedoreact.service.SpotifyService;
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

    public SpotifyController(SpotifyService service, UserService userService) {
        this.service = service;
        this.userService = userService;
    }

    @GetMapping("/auth")
    public ResponseEntity<String> getAuth() {
        return ResponseEntity.ok(service.getUrl());
    }

    @PostMapping("/token")
    public Mono<ResponseEntity<?>> setToken(@RequestBody AccessDTO accessDTO) {
        User user = service.getSpotifyUser(accessDTO);
        if(user == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid Token"));
        }
        return userService.save(user).map(ResponseEntity::ok);
    }
}
