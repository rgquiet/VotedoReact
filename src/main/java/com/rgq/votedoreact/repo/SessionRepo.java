package com.rgq.votedoreact.repo;

import com.rgq.votedoreact.dao.SessionDAO;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface SessionRepo extends ReactiveMongoRepository<SessionDAO, String> {

    Flux<SessionDAO> findAllByOpenAndNameLike(Boolean open, String name);
}
