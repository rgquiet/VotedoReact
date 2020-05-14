package com.rgq.votedoreact.controller;

import com.rgq.votedoreact.dto.CreateFriendDTO;
import com.rgq.votedoreact.dto.ExistDTO;
import com.rgq.votedoreact.dto.FriendDTO;
import com.rgq.votedoreact.model.User;
import com.rgq.votedoreact.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/friends/{id}")
    public Mono<ResponseEntity<List<FriendDTO>>> getFriendsById(@PathVariable String id) {
        return service.getById(id)
            .map(user -> user.getFriends())
            .flatMapMany(Flux::fromIterable)
            .flatMap(friendId -> service.getById(friendId))
            .map(friend -> new FriendDTO(friend.getId(), friend.getUsername(), friend.getImgUrl()))
            .collectList()
            .map(friends -> ResponseEntity.ok(friends));
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

    /* test sample */
    @GetMapping("/{id}")
    public Mono<User> getUserById(@PathVariable String id) {
        return service.getById(id);
    }
}
