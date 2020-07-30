package com.rgq.votedoreact.controller;

import com.rgq.votedoreact.dto.AccessDTO;
import com.rgq.votedoreact.dto.TrackDTO;
import com.rgq.votedoreact.model.User;
import com.rgq.votedoreact.service.SpotifyService;
import com.rgq.votedoreact.service.UserEventService;
import com.rgq.votedoreact.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/spotify")
public class SpotifyController {
    private final SpotifyService service;
    private final UserService userService;
    private final UserEventService userEventService;

    public SpotifyController(
        SpotifyService service,
        UserService userService,
        UserEventService userEventService
    ) {
        this.service = service;
        this.userService = userService;
        this.userEventService = userEventService;
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
        return userEventService.existCollectionWithName(user.getId()).flatMap(exist -> {
            // Check if user already exists in db
            if(exist) {
                return userService.getById(user.getId()).flatMap(response -> {
                    // Keep existing fields 'sessionId', 'trackId', 'friends' and 'votes'
                    user.setSessionId(response.getSessionId());
                    user.setTrackId(response.getTrackId());
                    user.setFriends(response.getFriends());
                    user.setVotes(response.getVotes());
                    return userService.save(user);
                });
            } else {
                // Create new capped Collection for user
                userEventService.createCollectionWithName(user.getId());
                return userService.save(user);
            }
        }).map(savedUser -> ResponseEntity.ok(userService.userDTOMapper(savedUser)));
    }

    @GetMapping("/devices/{userId}")
    public Mono<ResponseEntity<?>> getDevicesByUser(@PathVariable String userId) {
        return userService.getById(userId)
            .map(user -> service.getAvailableDevices(user.getAccessToken()))
            .map(response -> {
                if(response.get(0).getId().equals("No devices available")) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No devices available");
                } else {
                    return ResponseEntity.ok(response);
                }
            });
    }

    @GetMapping("/findTracks/{name}")
    public ResponseEntity<List<TrackDTO>> findTracksByName(@PathVariable String name) {
        return ResponseEntity.ok(service.searchTrackByName(name));
    }
}
