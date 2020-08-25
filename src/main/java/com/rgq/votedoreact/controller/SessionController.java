package com.rgq.votedoreact.controller;

import com.rgq.votedoreact.config.SessionEventPublisher;
import com.rgq.votedoreact.dao.SessionDAO;
import com.rgq.votedoreact.dto.*;
import com.rgq.votedoreact.dao.TrackDAO;
import com.rgq.votedoreact.dao.VoteDAO;
import com.rgq.votedoreact.service.*;
import com.wrapper.spotify.enums.ProductType;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

@RestController
@RequestMapping("/api/session")
public class SessionController {
    private final SessionService service;
    private final SessionEventService eventService;
    private final SchedulingService schedulingService;
    private final SpotifyService spotifyService;
    private final UserService userService;

    public SessionController(
        SessionService service,
        SessionEventService eventService,
        SchedulingService schedulingService,
        SpotifyService spotifyService,
        UserService userService
        ) {
        this.service = service;
        this.eventService = eventService;
        this.schedulingService = schedulingService;
        this.spotifyService = spotifyService;
        this.userService = userService;
    }

    @GetMapping(value = "/sub/{name}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<?>> subEventStream(@PathVariable String name) {
        return eventService.getPublishers().get(name).subPublisher();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<JoinSessionDTO>> getSessionById(@PathVariable String id) {
        return service.getById(id)
            .map(session -> ResponseEntity.ok(new JoinSessionDTO(
                session.getId(),
                session.getName(),
                null,
                spotifyService.currentTrackDTOMapper(session.getCurrentTrack())
            )))
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
    public Mono<ResponseEntity<String>> leaveSession(@PathVariable String id) {
        return userService.getById(id)
            .flatMap(user -> service.getById(user.getSessionId())
                .map(session -> {
                    if(session.getOwner().getId().equals(id)) {
                        schedulingService.removeMonitoredSession(session.getId());
                        service.closeSession(session);
                    } else {
                        // Clean up session
                        service.returnVotesByTrack(session, user.getTrackId());
                        session.getVotes().removeIf(vote ->
                            // wip: Update any track this user votes for
                            vote.getUserId().equals(user.getId()) ||
                            vote.getTrackId().equals(user.getTrackId())
                        );
                        session.getMembers().removeIf(member -> member.getId().equals(user.getId()));
                        service.save(session).subscribe();
                        // Clean up user
                        service.sendTrackRemoveEvent(user.getSessionId(), user.getTrackId());
                        user.setSessionId(null);
                        user.setTrackId(null);
                        user.setVotes(0);
                        userService.save(user).subscribe();
                    }
                    return "Session leaved";
                })
            ).map(ResponseEntity::ok);
    }

    @PostMapping("/restart")
    public void restartSession(@RequestBody StopTrackDTO stopTrackDTO) {
        userService.getById(stopTrackDTO.getOwnerId())
            .subscribe(owner -> service.getById(owner.getSessionId())
                .subscribe(session -> {
                    spotifyService.restartPlaybackOnDevice(
                        owner.getAccessToken(),
                        session.getDeviceId(),
                        stopTrackDTO.getTrackId(),
                        stopTrackDTO.getProgressMs()
                    );
                    final TrackDAO track = spotifyService.getPlaybackStatus(owner.getAccessToken());
                    track.setTimestamp(track.getTimestamp() - stopTrackDTO.getProgressMs());
                    service.sendTrackStartEvent(session.getId(), spotifyService.currentTrackDTOMapper(track));
                    session.setCurrentTrack(track);
                    // Add new session to the scheduling service
                    service.save(session)
                        .subscribe(saved -> schedulingService.getMonitoredSessions().put(saved, null));
            })
        );
    }

    @PostMapping("/join")
    public Mono<ResponseEntity<?>> joinSession(@RequestBody UserDTO userDTO) {
        return service.getById(userDTO.getSessionId())
            .switchIfEmpty(Mono.just(new SessionDAO()))
            .flatMap(session -> {
                if(session.getId() == null) {
                    return Mono.just("Session closed");
                }
                return userService.getById(userDTO.getId())
                    .flatMap(user -> {
                        if(user.getSessionId() == null) {
                            user.setVotes(1);
                            user.setSessionId(userDTO.getSessionId());
                            return userService.save(user)
                                .map(saved -> {
                                    session.getMembers().add(saved);
                                    service.save(session).subscribe();
                                    return new JoinSessionDTO(
                                        session.getId(),
                                        session.getName(),
                                        saved.getVotes(),
                                        spotifyService.currentTrackDTOMapper(session.getCurrentTrack())
                                    );
                                });
                        }
                        // User already in a session
                        return Mono.just(user.getSessionId());
                    });
            }).map(response -> {
                if(response instanceof JoinSessionDTO) {
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
                    if(user.getProduct() == ProductType.PREMIUM) {
                        spotifyService.startPlaybackOnDevice(user.getAccessToken(), createSessionDTO.getDeviceId());
                        final TrackDAO track = spotifyService.getPlaybackStatus(user.getAccessToken());
                        final Boolean open = (createSessionDTO.getInvitations() == null);
                        return service.save(new SessionDAO(
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
                            user.setVotes(1);
                            user.setSessionId(session.getId());
                            userService.save(user).subscribe();
                            // Create a SSE publisher for the new session
                            eventService.getPublishers().put(session.getId(), new SessionEventPublisher());
                            // Add new session to the scheduling service
                            schedulingService.getMonitoredSessions().put(session, null);
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
                            return new JoinSessionDTO(
                                session.getId(),
                                session.getName(),
                                user.getVotes(),
                                spotifyService.currentTrackDTOMapper(session.getCurrentTrack())
                            );
                        });
                    }
                    return Mono.just("Premium needed");
                }
                // User already in a session
                return Mono.just(user.getSessionId());
            }).map(response -> {
                if(response instanceof JoinSessionDTO) {
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

    @PostMapping("/revokeTrack")
    public void revokeTrack(@RequestBody VoteDTO voteDTO) {
        userService.getById(voteDTO.getUserId())
            .subscribe(user -> {
                service.getById(user.getSessionId())
                    .subscribe(session -> {
                        service.returnVotesByTrack(session, voteDTO.getTrackId());
                        session.getVotes().removeIf(vote -> vote.getTrackId().equals(voteDTO.getTrackId()));
                        service.save(session).subscribe();
                    });
                user.setTrackId(null);
                userService.save(user)
                    .subscribe(saved -> service.sendTrackRemoveEvent(saved.getSessionId(), voteDTO.getTrackId()));
            });
    }

    @PostMapping("/vote")
    public Mono<ResponseEntity<?>> voteForTrack(@RequestBody VoteDTO voteDTO) {
        return userService.getById(voteDTO.getUserId())
            .map(user -> {
                if(user.getVotes() > 0 && user.getSessionId() != null
                && !Objects.equals(user.getTrackId(), voteDTO.getTrackId())) {
                    service.getById(user.getSessionId())
                        .subscribe(session -> {
                            session.getVotes().add(new VoteDAO(
                                UUID.randomUUID(),
                                user.getId(),
                                voteDTO.getTrackId()
                            ));
                            service.save(session)
                                .subscribe(saved -> service.sendVoteTrackEvent(
                                    saved.getId(),
                                    service.sessionTrackDTOMapper(
                                        spotifyService.getTrackById(voteDTO.getTrackId()),
                                        saved.getVotes(),
                                        user.getId()
                                    )
                                ));
                        });
                    return userService.decVote(user);
                }
                return "Invalid vote";
            }).map(response -> {
                if(response.equals("Invalid vote")) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
                }
                return ResponseEntity.ok(response);
            });
    }
}
