package com.rgq.votedoreact.service;

import com.rgq.votedoreact.dto.*;
import com.rgq.votedoreact.model.Track;
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
    private final SessionEventService eventService;

    public SessionService(
        SessionRepo repo,
        UserService userService,
        UserEventService userEventService,
        SessionEventService eventService
    ) {
        this.repo = repo;
        this.userService = userService;
        this.userEventService = userEventService;
        this.eventService = eventService;
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

    public void sendSessionStopEvent(Session session, Integer timeMs) {
        eventService.getPublishers().get(session.getId())
            .publishEvent(new SessionSSE(
                EventType.SESSIONSTOP,
                new StopTrackDTO(
                    session.getOwner().getId(),
                    session.getCurrentTrack().getTrackInfos().getId(),
                    timeMs
                )
            ));
    }

    public void sendSessionStopEvent(String sessionId, String text) {
        eventService.getPublishers().get(sessionId)
            .publishEvent(new SessionSSE(EventType.SESSIONSTOP, text));
    }

    public void sendInvitation(String sessionId, String sessionName, String username, String userId) {
        userEventService.saveOrUpdateEvent(
            new UserSSE(
                sessionId,
                true,
                username + " invites you to his private session: " + sessionName
            ),
            userId)
        .subscribe();
    }

    public void sendTrackStartEvent(String sessionId, CurrentTrackDTO dto) {
        eventService.getPublishers().get(sessionId)
            .publishEvent(new SessionSSE(EventType.TRACKSTART, dto));
    }

    public void removeTrackById(Session session, String trackId) {
        System.out.println(">>> Track: " + trackId + " <<<");
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
                        System.out.println(">>> User: " + user.getTrackId() + " <<<");
                        user.setTrackId(null);
                        userService.save(user).subscribe(saved -> {
                            // wip...
                            System.out.println(">>> Saved: " + saved.getTrackId() + " <<<");
                        });
                        check = true;
                        break;
                    }
                }
            }
        }
        if(check) {
            eventService.getPublishers().get(session.getId())
                .publishEvent(new SessionSSE(
                    EventType.TRACKREMOVE, trackId
                ));
        }
    }

    public Boolean trackAlreadyUsed(Session session, String trackId) {
        if(session.getOwner().getTrackId() != null) {
            if(session.getOwner().getTrackId().equals(trackId)) {
                return true;
            }
        }
        for(User user : session.getMembers()) {
            if(user.getTrackId() != null) {
                if(user.getTrackId().equals(trackId)) {
                    return true;
                }
            }
        }
        if(session.getCurrentTrack().getTrackInfos().getId() != null) {
            if(session.getCurrentTrack().getTrackInfos().getId().equals(trackId)) {
                return true;
            }
        }
        for(Track track : session.getPlayedTracks()) {
            if(track.getTrackInfos().getId() != null) {
                if(track.getTrackInfos().getId().equals(trackId)) {
                    return true;
                }
            }
        }
        return false;
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

    public String selectRandomTrack(Session session) {
        if(session.getPlayedTracks().isEmpty()) {
            return session.getCurrentTrack().getTrackInfos().getId();
        }
        int i = new Random().nextInt(session.getPlayedTracks().size());
        return session.getPlayedTracks().get(i).getTrackInfos().getId();
    }

    public void sendVoteStopEvent(String sessionId) {
        // wip: Put something other than null in dto
        eventService.getPublishers().get(sessionId)
            .publishEvent(new SessionSSE(
                EventType.VOTESTOP, null
            ));
    }

    public void distributeNewVotes(Session session) {
        userService.incVote(session.getOwner());
        session.getMembers().forEach(userService::incVote);
    }

    public List<Vote> cleanUpVotes(List<Vote> votes, String trackId) {
        votes.removeIf(vote -> vote.getTrackId().equals(trackId));
        return votes;
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
