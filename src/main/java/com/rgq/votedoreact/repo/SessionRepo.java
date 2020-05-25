package com.rgq.votedoreact.repo;

import com.rgq.votedoreact.model.Session;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Tailable;
import reactor.core.publisher.Flux;

public interface SessionRepo extends ReactiveMongoRepository<Session, String> {

    @Tailable
    Flux<Session> findByOpen(Boolean open);
}
