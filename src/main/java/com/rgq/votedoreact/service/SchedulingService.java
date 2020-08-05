package com.rgq.votedoreact.service;

import com.rgq.votedoreact.model.Session;
import com.rgq.votedoreact.model.Track;
import com.rgq.votedoreact.repo.SessionRepo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class SchedulingService {
    private final SessionService service;
    private final SpotifyService spotifyService;
    private final ArrayList<Session> monitoredSessions;

    public SchedulingService(
        SessionRepo repo,
        SessionService service,
        SpotifyService spotifyService
    ) {
        this.service = service;
        this.spotifyService = spotifyService;
        this.monitoredSessions = new ArrayList<>();
        // wip: repo.findAll().subscribe(monitoredSessions::add);
    }

    public ArrayList<Session> getMonitoredSessions() {
        return monitoredSessions;
    }

    @Scheduled(fixedRate = 5000)
    private void checkPlaybackStatus() {
        // wip...
        monitoredSessions.forEach(session -> {
            Track track = spotifyService.getPlaybackStatus(session.getOwner().getAccessToken());
            if(track != null) {
                Long timestamp1 = session.getCurrentTrack().getTimestamp() - session.getCurrentTrack().getProgressMs();
                Long timestamp2 = track.getTimestamp() - track.getProgressMs();
                if(timestamp2 - timestamp1 < 2000 &&
                session.getCurrentTrack().getTrackInfos().getId().equals(track.getTrackInfos().getId())) {
                    if(track.getTrackInfos().getTimeMs() - track.getProgressMs() < 5000) {
                        service.evaluateNextTrack(session);
                    }
                } else {
                    System.out.println(">>> " + (timestamp2 - timestamp1) + " <<<");
                    System.out.println(">>> " + track.getTrackInfos().getName() + " <<<");
                }
            } else {
                System.out.println(">>> Stop session: null <<<");
            }
        });
    }
}
