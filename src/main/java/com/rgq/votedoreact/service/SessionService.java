package com.rgq.votedoreact.service;

import com.rgq.votedoreact.dto.SessionDTO;
import com.rgq.votedoreact.dto.SessionTrackDTO;
import com.rgq.votedoreact.dto.TrackDTO;
import com.rgq.votedoreact.model.Vote;
import com.rgq.votedoreact.sse.UserSSE;
import com.rgq.votedoreact.model.Session;
import com.rgq.votedoreact.repo.SessionRepo;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class SessionService {
    private SessionRepo repo;
    private UserEventService userEventService;

    public SessionService(SessionRepo repo, UserEventService userEventService) {
        this.repo = repo;
        this.userEventService = userEventService;
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
        userEventService.saveOrUpdateEvent(
            new UserSSE(
                sessionId,
                true,
                username + " invites you to his private session: " + session
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

    public SessionTrackDTO sessionTrackDTOMapper(TrackDTO track, List<Vote> votes, String userId) {
        Integer trackVote = 0;
        // Counts votes of the song in the session
        for(int i = 0; i < votes.size(); i++) {
            if(votes.get(i).getTrackId().equals(track.getId())) {
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
