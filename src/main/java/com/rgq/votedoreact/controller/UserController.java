package com.rgq.votedoreact.controller;

import com.rgq.votedoreact.dto.*;
import com.rgq.votedoreact.service.SessionService;
import com.rgq.votedoreact.sse.UserSSE;
import com.rgq.votedoreact.dao.UserDAO;
import com.rgq.votedoreact.service.UserEventService;
import com.rgq.votedoreact.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService service;
    private final UserEventService eventService;
    private final SessionService sessionService;

    public UserController(
        UserService service,
        UserEventService eventService,
        SessionService sessionService
    ) {
        this.service = service;
        this.eventService = eventService;
        this.sessionService = sessionService;
    }

    @GetMapping(value = "/sub/{name}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<UserSSE> subEventStream(@PathVariable String name) {
        return eventService.subCollectionByName(name);
    }

    @GetMapping("/findUser/{name}")
    public Flux<UserDTO> findUser(@PathVariable String name) {
        if(name.equals("_")) {
            name = "";
        }
        return service.getByUsernameLike(name).map(service::userDTOMapper);
    }

    @GetMapping("/friends/{id}")
    public Mono<ResponseEntity<List<FriendDTO>>> getFriendsById(@PathVariable String id) {
        return service.getById(id)
            .map(UserDAO::getFriends)
            .flatMapMany(Flux::fromIterable)
            .flatMap(service::getById)
            .map(friend -> new FriendDTO(friend.getId(), friend.getUsername(), friend.getImgUrl()))
            .collectList()
            .map(ResponseEntity::ok);
    }

    @PostMapping("/addFriend")
    public Mono<ResponseEntity<String>> addFriend(@RequestBody EditFriendDTO friendDTO) {
        return service.getById(friendDTO.getFriendId())
            .switchIfEmpty(Mono.just(new UserDAO()))
            .map(friend -> {
                // Check if new friend is a user that exists
                if(friend.getId() == null) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No user with this id");
                }
                // Get user to save new friend
                service.getById(friendDTO.getId())
                    .subscribe(user -> {
                        // Check if user already saved this friend
                        ExistDTO existDTO = new ExistDTO(false);
                        user.getFriends().forEach(id -> {
                            if(id.equals(friendDTO.getFriendId())) {
                                existDTO.setExist(true);
                            }
                        });
                        if(!existDTO.getExist()) {
                            // Set new friend and save affected user
                            user.getFriends().add(friendDTO.getFriendId());
                            service.save(user).subscribe();
                        }
                    });
                return ResponseEntity.ok(friendDTO.getFriendId() + " saved");
            });
    }

    @PostMapping("/removeFriend")
    public Mono<ResponseEntity<String>> removeFriend(@RequestBody EditFriendDTO friendDTO) {
        return service.getById(friendDTO.getId())
            .flatMap(user -> {
                if(user.getFriends().removeIf(friend -> friend.equals(friendDTO.getFriendId()))) {
                    return service.save(user).map(saved -> friendDTO.getId() + " removed");
                }
                return Mono.just("No user with this id");
            }).map(response -> {
                if(response.equals("No user with this id")) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
                }
                return ResponseEntity.ok(response);
            });
    }

    @PostMapping("/track/{id}")
    public Mono<ResponseEntity<String>> setTrack(@PathVariable String id, @RequestBody TrackDTO trackDTO) {
        return service.getById(id)
            .flatMap(user -> {
                if(user.getTrackId() == null) {
                    return sessionService.getById(user.getSessionId())
                        .map(session -> {
                            if(sessionService.trackAlreadyUsed(session, trackDTO.getId())) {
                                return "Track already used";
                            }
                            user.setTrackId(trackDTO.getId());
                            service.save(user)
                                .subscribe(saved -> sessionService.sendTrackCreateEvent(
                                    saved.getSessionId(),
                                    new SessionTrackDTO(
                                        trackDTO.getId(),
                                        saved.getId(),
                                        trackDTO.getName(),
                                        trackDTO.getArtist(),
                                        trackDTO.getImgUrl(),
                                        trackDTO.getTimeMs(),
                                        0
                                    )
                                ));
                            return trackDTO.getId();
                        });
                }
                return Mono.just(user.getTrackId());
            }).map(response -> {
                if(response.equals(trackDTO.getId())) {
                    return ResponseEntity.ok(response);
                }
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            });
    }

    @PostMapping("/closeEvent")
    public void closeEvent(@RequestBody UserDTO dto) {
        eventService.getById(dto.getSessionId(), dto.getId())
            .subscribe(userSSE -> {
                userSSE.setStatus(false);
                eventService.saveOrUpdateEvent(userSSE, dto.getId()).subscribe();
            });
    }
}
