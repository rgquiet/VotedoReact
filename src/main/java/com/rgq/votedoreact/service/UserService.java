package com.rgq.votedoreact.service;

import com.rgq.votedoreact.config.TestUserConfig;
import com.rgq.votedoreact.model.User;
import com.rgq.votedoreact.repo.UserRepo;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserService {
    private User testUser;
    private UserRepo repo;

    public UserService(TestUserConfig config, UserRepo repo) {
        this.testUser = config.createTestUser();
        this.repo = repo;
    }

    public Mono<User> getById(String id) {
        return repo.findById(id);
    }

    public User getTest() {
        return testUser;
    }
}
