package com.rgq.votedoreact.repo;

import com.rgq.votedoreact.model.Session;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface SessionRepo extends ReactiveMongoRepository<Session, String> { }
