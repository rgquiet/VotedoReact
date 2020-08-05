package com.rgq.votedoreact.service;

import com.rgq.votedoreact.dto.*;
import com.rgq.votedoreact.model.Vote;
import com.rgq.votedoreact.sse.EventType;
import com.rgq.votedoreact.sse.SessionSSE;
import com.rgq.votedoreact.sse.UserSSE;
import com.rgq.votedoreact.model.Session;
import com.rgq.votedoreact.repo.SessionRepo;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class SessionService {
    private final SessionRepo repo;
    private final UserService userService;
    private final UserEventService userEventService;
    private final SessionEventService sessionEventService;

    public SessionService(
        SessionRepo repo,
        UserService userService,
        UserEventService userEventService,
        SessionEventService sessionEventService
    ) {
        this.repo = repo;
        this.userService = userService;
        this.userEventService = userEventService;
        this.sessionEventService = sessionEventService;
    }

    public Mono<Session> save(Session session) {
        return repo.save(session);
    }

    public Mono<Session> getById(String id) {
        return repo.findById(id);
    }

    public Flux<Session> getOpenByNameLike(String name) {
        return repo.findAllByOpenAndNameLike(true, name).limitRequest(10);
    }

    public void sendInvitation(String sessionId, String session, String username, String userId) {
        userEventService.saveOrUpdateEvent(
            new UserSSE(
                sessionId,
                true,
                username + " invites you to his private session: " + session
            ),
            userId)
        .subscribe();
    }

    public void evaluateNextTrack(Session session) {
        // wip...
        // Distribute new vote to each session member
        userService.incVote(session.getOwner());
        session.getMembers().forEach(userService::incVote);
        sessionEventService.getPublishers().get(session.getId())
            .publishEvent(new SessionSSE(
                EventType.VOTENEW,
                null
            ));
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

    public SessionTrackDTO sessionTrackDTOMapper(TrackDTO track, List<Vote> votes, String userId) {
        Integer trackVote = 0;
        // Counts votes of the song in the session
        for(Vote vote : votes) {
            if (vote.getTrackId().equals(track.getId())) {
                trackVote++;
            }
        }
        return new SessionTrackDTO(
            track.getId(),
            userId,
            track.getName(),
            track.getArtist(),
            track.getImgUrl(),
            track.getTimeMs(),
            trackVote
        );
    }
}
