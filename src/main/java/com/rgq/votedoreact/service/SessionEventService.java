package com.rgq.votedoreact.service;

import com.rgq.votedoreact.config.SessionEventPublisher;
import com.rgq.votedoreact.repo.SessionRepo;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Getter
@Service
public class SessionEventService {
    private final HashMap<String, SessionEventPublisher> publishers;

    public SessionEventService(SessionRepo repo) {
        this.publishers = new HashMap<>();
        repo.findAll().subscribe(session -> publishers.put(session.getId(), new SessionEventPublisher()));
    }
}
