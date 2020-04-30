package com.rgq.votedoreact.service;

import com.rgq.votedoreact.model.Session;
import com.rgq.votedoreact.repo.SessionRepo;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class SessionService {
    private SessionRepo repo;

    public SessionService(SessionRepo repo) {
        this.repo = repo;
    }

    public Mono<Session> getById(String id) {
        return repo.findById(id);
    }
}
