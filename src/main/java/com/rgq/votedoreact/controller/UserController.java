package com.rgq.votedoreact.controller;

import com.rgq.votedoreact.dto.*;
import com.rgq.votedoreact.service.SessionEventService;
import com.rgq.votedoreact.sse.EventType;
import com.rgq.votedoreact.sse.SessionSSE;
import com.rgq.votedoreact.sse.UserSSE;
import com.rgq.votedoreact.model.User;
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
    private final SessionEventService sessionEventService;

    public UserController(
        UserService service,
        UserEventService eventService,
        SessionEventService sessionEventService
    ) {
        this.service = service;
        this.eventService = eventService;
        this.sessionEventService = sessionEventService;
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
            .map(User::getFriends)
            .flatMapMany(Flux::fromIterable)
            .flatMap(service::getById)
            .map(friend -> new FriendDTO(friend.getId(), friend.getUsername(), friend.getImgUrl()))
            .collectList()
            .map(ResponseEntity::ok);
    }

    @PostMapping("/friend")
    public Mono<ResponseEntity<String>> setFriend(@RequestBody CreateFriendDTO createFriendDTO) {
        return service.getById(createFriendDTO.getFriendId())
            .switchIfEmpty(Mono.just(new User()))
            .map(friend -> {
                // Check if new friend is a user that exists
                if(friend.getId() == null) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("no user with this id");
                } else {
                    // Get user to save new friend
                    service.getById(createFriendDTO.getId())
                        .subscribe(user -> {
                            // Check if user already saved this friend
                            ExistDTO existDTO = new ExistDTO(false);
                            user.getFriends().forEach(id -> {
                                if(id.equals(createFriendDTO.getFriendId())) {
                                    existDTO.setExist(true);
                                }
                            });
                            if(!existDTO.getExist()) {
                                // Set new friend and save affected user
                                user.getFriends().add(createFriendDTO.getFriendId());
                                service.save(user).subscribe();
                            }
                        });
                    return ResponseEntity.ok(createFriendDTO.getFriendId() + " saved");
                }
            });
    }

    @PostMapping("/track/{id}")
    public Mono<ResponseEntity<String>> setTrack(@PathVariable String id, @RequestBody TrackDTO trackDTO) {
        return service.getById(id).map(user -> {
            if(user.getTrackId() == null) {
                user.setTrackId(trackDTO.getId());
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(user.getTrackId());
            }
            SessionTrackDTO sessionTrack = new SessionTrackDTO(
                trackDTO.getId(),
                user.getId(),
                trackDTO.getName(),
                trackDTO.getArtist(),
                trackDTO.getImgUrl(),
                trackDTO.getTimeMs(),
                0
            );
            service.save(user).subscribe(saved -> sessionEventService
                .getPublishers()
                .get(saved.getSessionId())
                .publishEvent(new SessionSSE(EventType.TRACKCREATE, sessionTrack)));
            return ResponseEntity.ok(trackDTO.getId() + " saved");
        });
    }

    @PostMapping("/closeEvent")
    public void closeEvent(@RequestBody UserDTO dto) {
        eventService.getById(dto.getSessionId(), dto.getId()).subscribe(userSSE -> {
            userSSE.setStatus(false);
            eventService.saveOrUpdateEvent(userSSE, dto.getId()).subscribe();
        });
    }
}
