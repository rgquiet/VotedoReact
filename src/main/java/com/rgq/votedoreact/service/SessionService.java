package com.rgq.votedoreact.service;

import com.rgq.votedoreact.dto.SessionDTO;
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

    public Mono<Session> save(Session session) {
        return repo.save(session);
    }

    public SessionDTO sessionDTOMapper(Session session) {
        return new SessionDTO(session.getId(), session.getName());
    }
}
