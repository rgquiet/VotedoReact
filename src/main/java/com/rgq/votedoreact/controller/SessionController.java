package com.rgq.votedoreact.controller;

import com.rgq.votedoreact.config.SessionEventPublisher;
import com.rgq.votedoreact.dto.*;
import com.rgq.votedoreact.model.Session;
import com.rgq.votedoreact.model.Track;
import com.rgq.votedoreact.service.*;
import com.rgq.votedoreact.sse.EventType;
import com.rgq.votedoreact.sse.SessionSSE;
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
    private final SessionService service;
    private final SessionEventService eventService;
    private final SchedulingService schedulingService;
    private final UserService userService;
    private final SpotifyService spotifyService;

    public SessionController(
        SessionService service,
        SessionEventService eventService,
        SchedulingService schedulingService,
        UserService userService,
        SpotifyService spotifyService
    ) {
        this.service = service;
        this.eventService = eventService;
        this.schedulingService = schedulingService;
        this.userService = userService;
        this.spotifyService = spotifyService;
    }

    @GetMapping(value = "/sub/{name}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<?>> subEventStream(@PathVariable String name) {
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
        return service.getOpenByNameLike(name).map(service::sessionDTOMapper);
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
                    spotifyService.startPlaybackOnDevice(user.getAccessToken(), createSessionDTO.getDeviceId());
                    final Track track = spotifyService.getPlaybackStatus(user.getAccessToken());
                    final Boolean open = (createSessionDTO.getInvitations() == null);
                    return service.save(new Session(
                        null,
                        createSessionDTO.getDeviceId(),
                        createSessionDTO.getName(),
                        open,
                        user,
                        new ArrayList<>(),
                        track,
                        new ArrayList<>(),
                        new ArrayList<>()
                    )).map(session -> {
                        user.setSessionId(session.getId());
                        userService.save(user).subscribe();
                        // Create a SSE publisher for the new session
                        eventService.getPublishers().put(session.getId(), new SessionEventPublisher());
                        eventService.getPublishers().get(session.getId())
                            .publishEvent(new SessionSSE(
                                EventType.TRACKSTART,
                                new CurrentTrackDTO(
                                    track.getTrackInfos().getId(),
                                    track.getTrackInfos().getName(),
                                    track.getTrackInfos().getArtist(),
                                    track.getTrackInfos().getImgUrl(),
                                    track.getTrackInfos().getTimeMs(),
                                    track.getTimestamp()
                                )
                            ));
                        // Add new session to the scheduling service
                        schedulingService.getMonitoredSessions().add(session);
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
