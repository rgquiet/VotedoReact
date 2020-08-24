package com.rgq.votedoreact.repo;

import com.rgq.votedoreact.dao.UserDAO;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface UserRepo extends ReactiveMongoRepository<UserDAO, String> {

    Flux<UserDAO> findAllByUsernameLike(String name);
}
