package com.rgq.votedoreact.service;

import com.rgq.votedoreact.dto.*;
import com.rgq.votedoreact.model.User;
import com.rgq.votedoreact.model.Vote;
import com.rgq.votedoreact.sse.EventType;
import com.rgq.votedoreact.sse.SessionSSE;
import com.rgq.votedoreact.sse.UserSSE;
import com.rgq.votedoreact.model.Session;
import com.rgq.votedoreact.repo.SessionRepo;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

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
        // wip: https://stackoverflow.com/questions/9040161/mongo-order-by-length-of-array
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

    public void removeTrackById(Session session, String trackId) {
        boolean check = false;
        if(session.getOwner().getTrackId() != null) {
            if(session.getOwner().getTrackId().equals(trackId)) {
                session.getOwner().setTrackId(null);
                userService.save(session.getOwner()).subscribe();
                check = true;
            }
        }
        if(!check) {
            for(User user : session.getMembers()) {
                if(user.getTrackId() != null) {
                    if(user.getTrackId().equals(trackId)) {
                        user.setTrackId(null);
                        userService.save(user).subscribe();
                        check = true;
                        break;
                    }
                }
            }
        }
        if(check) {
            sessionEventService.getPublishers().get(session.getId())
                .publishEvent(new SessionSSE(
                    EventType.TRACKREMOVE, trackId
                ));
        }
    }

    public String evaluateNextTrack(Session session) {
        // Get trackId with the most votes
        HashMap<String, Integer> ranking = new HashMap<>();
        session.getVotes().forEach(vote -> ranking.merge(vote.getTrackId(), 1, Integer::sum));
        if(ranking.isEmpty()) {
            return null;
        }
        Map.Entry<String, Integer> winner = Collections.max(ranking.entrySet(), Map.Entry.comparingByValue());
        return winner.getKey();
    }

    public List<Vote> cleanUpVotes(List<Vote> votes, String trackId) {
        votes.removeIf(vote -> vote.getTrackId().equals(trackId));
        return votes;
    }

    public void distributeNewVotes(Session session) {
        userService.incVote(session.getOwner());
        session.getMembers().forEach(userService::incVote);
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
            if(vote.getTrackId().equals(track.getId())) {
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
