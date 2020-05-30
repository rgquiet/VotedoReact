package com.rgq.votedoreact.service;

import com.rgq.votedoreact.dto.SessionDTO;
import com.rgq.votedoreact.model.Event;
import com.rgq.votedoreact.model.EventType;
import com.rgq.votedoreact.model.Session;
import com.rgq.votedoreact.repo.SessionRepo;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class SessionService {
    private SessionRepo repo;
    private SseService sse;

    public SessionService(SessionRepo repo, SseService sse) {
        this.repo = repo;
        this.sse = sse;
    }

    public Mono<Session> save(Session session) {
        return repo.save(session);
    }

    public Mono<Session> getById(String id) {
        return repo.findById(id);
    }

    public Flux<Session> getOpenByNameLike(String name) {
        return repo.findAllByOpenAndNameLike(true, name);
    }

    public void sendInvitation(String sessionId, String session, String username, String userId) {
        sse.saveOrUpdateEvent(
            new Event(
                sessionId,
                false,
                EventType.SESSION,
                username + " invites you to his private Session " + session
            ),
            userId)
        .subscribe();
    }

    public SessionDTO sessionDTOMapper(Session session) {
        return new SessionDTO(
            session.getId(),
            session.getName(),
            session.getOpen(),
            session.getOwner().getUsername(),
            session.getOwner().getImgUrl(),
            session.getMembers().size() + 1
        );
    }
}
