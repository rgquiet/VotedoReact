package com.rgq.votedoreact.controller;

import com.rgq.votedoreact.config.SessionEventPublisher;
import com.rgq.votedoreact.dto.*;
import com.rgq.votedoreact.model.Session;
import com.rgq.votedoreact.service.SessionEventService;
import com.rgq.votedoreact.service.SessionService;
import com.rgq.votedoreact.service.SpotifyService;
import com.rgq.votedoreact.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/session")
public class SessionController {
    private SessionService service;
    private SessionEventService eventService;
    private UserService userService;
    private SpotifyService spotifyService;

    public SessionController(
        SessionService service,
        SessionEventService eventService,
        UserService userService,
        SpotifyService spotifyService
    ) {
        this.service = service;
        this.eventService = eventService;
        this.userService = userService;
        this.spotifyService = spotifyService;
    }

    @GetMapping(value = "/sub/{name}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent> subEventStream(@PathVariable String name) {
        return eventService.getPublishers().get(name).subPublisher();
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
                    final Boolean open = (createSessionDTO.getInvitations() == null);
                    return service.save(new Session(
                        null,
                        createSessionDTO.getDeviceId(),
                        createSessionDTO.getName(),
                        open,
                        user,
                        new ArrayList<>(),
                        new ArrayList<>()
                    )).map(session -> {
                        eventService.getPublishers().put(session.getId(), new SessionEventPublisher());
                        user.setSessionId(session.getId());
                        userService.save(user).subscribe();
                        if(!open) {
                            for(int i = 0; i < createSessionDTO.getInvitations().length; i++) {
                                service.sendInvitation(
                                    session.getId(),
                                    session.getName(),
                                    user.getUsername(),
                                    createSessionDTO.getInvitations()[i]
                                );
                            }
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

    @GetMapping("/getTracks/{id}")
    public Mono<List<SessionTrackDTO>> getTracksById(@PathVariable String id) {
        return service.getById(id)
            .map(session -> {
                List<SessionTrackDTO> tracks = new ArrayList<>();
                String ownerTrackId = session.getOwner().getTrackId();
                /* wip: Change model.Session
                    - owner field just holds userId
                    - members list contains user object of owner
                    - sessionDTOMapper must be changed (owner.getImgUrl(), getUsername())
                */
                if(ownerTrackId != null) {
                    TrackDTO dto = spotifyService.getTrackById(ownerTrackId);
                    tracks.add(
                        service.sessionTrackDTOMapper(dto, session.getVotes(), session.getOwner().getId())
                    );
                }
                session.getMembers().forEach(user -> {
                    if(user.getTrackId() != null) {
                        TrackDTO dto = spotifyService.getTrackById(user.getTrackId());
                        tracks.add(
                            service.sessionTrackDTOMapper(dto, session.getVotes(), user.getId())
                        );
                    }
                });
                return tracks;
            });
    }
}
