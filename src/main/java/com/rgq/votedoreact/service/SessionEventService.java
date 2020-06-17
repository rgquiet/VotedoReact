package com.rgq.votedoreact.service;

import com.rgq.votedoreact.config.SessionEventPublisher;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Getter
@Service
public class SessionEventService {
    private final HashMap<String, SessionEventPublisher> publishers;

    public SessionEventService() {
        this.publishers = new HashMap<>();
    }
}
