package com.rgq.votedoreact.service;

import com.rgq.votedoreact.model.Session;
import com.rgq.votedoreact.model.Track;
import com.rgq.votedoreact.repo.SessionRepo;
import com.rgq.votedoreact.sse.EventType;
import com.rgq.votedoreact.sse.SessionSSE;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Random;

@Service
public class SchedulingService {
    private final SessionService service;
    private final SessionEventService eventService;
    private final SpotifyService spotifyService;
    private final HashMap<Session, String> monitoredSessions;

    public SchedulingService(
        SessionRepo repo,
        SessionService service,
        SessionEventService eventService,
        SpotifyService spotifyService
    ) {
        this.service = service;
        this.eventService = eventService;
        this.spotifyService = spotifyService;
        this.monitoredSessions = new HashMap<>();
        repo.findAll().subscribe(session -> monitoredSessions.put(session, null));
    }

    public HashMap<Session, String> getMonitoredSessions() {
        return monitoredSessions;
    }

    @Scheduled(fixedRate = 5000)
    private void checkPlaybackStatus() {
        monitoredSessions.forEach((session, trackId) -> {
            Track track = spotifyService.getPlaybackStatus(session.getOwner().getAccessToken());
            if(track != null) {
                if(trackId != null) {
                    service.getById(session.getId()).subscribe(update -> {
                        // Update session and alert all user about next track
                        service.removeTrackById(update, trackId);
                        update.setVotes(service.cleanUpVotes(update.getVotes(), trackId));
                        update.getPlayedTracks().add(update.getCurrentTrack());
                        update.setCurrentTrack(track);
                        service.save(update).subscribe();
                    });
                    service.distributeNewVotes(session);
                    eventService.getPublishers().get(session.getId())
                        .publishEvent(new SessionSSE(
                            EventType.TRACKSTART, spotifyService.currentTrackDTOMapper(track)
                        ));
                    session.setCurrentTrack(track);
                    monitoredSessions.put(session, null);
                } else {
                    Long timestamp1 = session.getCurrentTrack().getTimestamp() - session.getCurrentTrack().getProgressMs();
                    Long timestamp2 = track.getTimestamp() - track.getProgressMs();
                    if(timestamp2 - timestamp1 < 2000 &&
                    session.getCurrentTrack().getTrackInfos().getId().equals(track.getTrackInfos().getId())) {
                        if(track.getTrackInfos().getTimeMs() - track.getProgressMs() < 5000) {
                            // Selects next track for this session
                            eventService.getPublishers().get(session.getId())
                                .publishEvent(new SessionSSE(
                                    EventType.VOTESTOP, null
                                ));
                            service.getById(session.getId())
                                .subscribe(update -> {
                                    String nextTrackId = service.evaluateNextTrack(update);
                                    if(nextTrackId == null) {
                                        if(update.getPlayedTracks().isEmpty()) {
                                            nextTrackId = update.getCurrentTrack().getTrackInfos().getId();
                                        } else {
                                            // Takes a random played track, because nobody vote for a new one
                                            int i = new Random().nextInt(update.getPlayedTracks().size());
                                            nextTrackId = update.getPlayedTracks().get(i).getTrackInfos().getId();
                                        }
                                    }
                                    spotifyService.addTrackToQueue(
                                        update.getOwner().getAccessToken(),
                                        update.getDeviceId(),
                                        nextTrackId
                                    );
                                    monitoredSessions.put(update, nextTrackId);
                                    monitoredSessions.remove(session);
                                });
                        }
                    } else {
                        eventService.getPublishers().get(session.getId())
                            .publishEvent(new SessionSSE(
                                EventType.SESSIONSTOP,
                                track.getTrackInfos().getName() + ": " + (timestamp2 - timestamp1)
                            ));
                    }
                }
            } else {
                eventService.getPublishers().get(session.getId())
                    .publishEvent(new SessionSSE(
                        EventType.SESSIONSTOP, "session null"
                    ));
            }
        });
    }
}
