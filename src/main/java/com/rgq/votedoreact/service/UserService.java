package com.rgq.votedoreact.service;

import com.rgq.votedoreact.dao.UserDAO;
import com.rgq.votedoreact.dto.UserDTO;
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

    public Mono<UserDAO> save(UserDAO user) {
        return repo.save(user);
    }

    public Mono<UserDAO> getById(String id) {
        return repo.findById(id);
    }

    public Flux<UserDAO> getByUsernameLike(String name) {
        return repo.findAllByUsernameLike(name).limitRequest(10);
    }

    public void incVote(UserDAO user) {
        Integer votes = user.getVotes() + 1;
        user.setVotes(votes);
        repo.save(user).subscribe();
    }

    public Integer decVote(UserDAO user) {
        Integer votes = user.getVotes() - 1;
        user.setVotes(votes);
        repo.save(user).subscribe();
        return votes;
    }

    public UserDTO userDTOMapper(UserDAO user) {
        return new UserDTO(
            user.getId(),
            user.getSessionId(),
            user.getUsername(),
            user.getImgUrl(),
            user.getVotes()
        );
    }
}
