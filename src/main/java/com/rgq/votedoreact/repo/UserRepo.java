package com.rgq.votedoreact.repo;

import com.rgq.votedoreact.model.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface UserRepo extends ReactiveMongoRepository<User, String> { }
