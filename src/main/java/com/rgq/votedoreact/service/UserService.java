package com.rgq.votedoreact.service;

import com.rgq.votedoreact.dto.UserDTO;
import com.rgq.votedoreact.model.User;
import com.rgq.votedoreact.repo.UserRepo;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserService {
    private UserRepo repo;

    public UserService(UserRepo repo) {
        this.repo = repo;
    }

    public Mono<User> getById(String id) {
        return repo.findById(id);
    }

    public Mono<User> save(User user) {
        return repo.save(user);
    }

    public UserDTO userDTOMapper(User user) {
        return new UserDTO(user.getId(), user.getUsername(), user.getImgUrl());
    }
}
