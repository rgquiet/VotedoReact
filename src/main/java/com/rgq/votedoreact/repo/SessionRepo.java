package com.rgq.votedoreact.repo;

import com.rgq.votedoreact.model.Session;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface SessionRepo extends ReactiveMongoRepository<Session, String> {

    Flux<Session> findAllByOpenAndNameLike(Boolean open, String name);
}
