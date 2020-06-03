package com.rgq.votedoreact.repo;

import com.rgq.votedoreact.model.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface UserRepo extends ReactiveMongoRepository<User, String> {

    Flux<User> findAllByUsernameLike(String name);
}
