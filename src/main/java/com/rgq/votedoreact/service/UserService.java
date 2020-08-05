package com.rgq.votedoreact.service;

import com.rgq.votedoreact.dto.UserDTO;
import com.rgq.votedoreact.model.User;
import com.rgq.votedoreact.repo.UserRepo;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UserService {
    private final UserRepo repo;

    public UserService(UserRepo repo) {
        this.repo = repo;
    }

    public Mono<User> save(User user) {
        return repo.save(user);
    }

    public Mono<User> getById(String id) {
        return repo.findById(id);
    }

    public Flux<User> getByUsernameLike(String name) {
        return repo.findAllByUsernameLike(name).limitRequest(10);
    }

    public void incVote(User user) {
        Integer votes = user.getVotes() + 1;
        user.setVotes(votes);
        repo.save(user).subscribe();
    }

    public UserDTO userDTOMapper(User user) {
        return new UserDTO(
            user.getId(),
            user.getSessionId(),
            user.getUsername(),
            user.getImgUrl(),
            user.getVotes()
        );
    }
}
