package com.rgq.votedoreact.service;

import com.rgq.votedoreact.model.Session;
import com.rgq.votedoreact.model.Track;
import com.rgq.votedoreact.repo.SessionRepo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class SchedulingService {
    private final SpotifyService spotifyService;
    private final ArrayList<Session> monitoredSessions;

    public SchedulingService(
        SessionRepo repo,
        SpotifyService spotifyService
    ) {
        this.spotifyService = spotifyService;
        this.monitoredSessions = new ArrayList<>();
        repo.findAll().subscribe(monitoredSessions::add);
    }

    public ArrayList<Session> getMonitoredSessions() {
        return monitoredSessions;
    }

    @Scheduled(fixedRate = 5000)
    private void checkPlaybackStatus() {
        monitoredSessions.forEach(session -> {
            Track track = spotifyService.getPlaybackStatus(session.getOwner().getAccessToken());
            if(track != null) {
                if(session.getCurrentTrack().getTimestamp().equals(track.getTimestamp())) {
                    if(track.getTrackInfos().getTimeMs() - track.getProgressMs() < 5000) {
                        System.out.println(">>> change track <<<");
                    } else {
                        System.out.println(">>> progress: "
                            + (track.getTrackInfos().getTimeMs() - track.getProgressMs()) +
                        " <<<");
                    }
                } else {
                    System.out.println(">>> current: " + session.getCurrentTrack().getTimestamp() + " <<<");
                    System.out.println(">>> api: " + track.getTimestamp() + " <<<");
                }
            } else {
                System.out.println(">>> Stop session: null <<<");
            }
        });
    }
}
