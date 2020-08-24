package com.rgq.votedoreact.service;

import com.rgq.votedoreact.dao.SessionDAO;
import com.rgq.votedoreact.dao.TrackDAO;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Service
public class SchedulingService {
    private final SessionService service;
    private final SpotifyService spotifyService;
    private final HashMap<SessionDAO, String> monitoredSessions;

    public SchedulingService(SessionService service, SpotifyService spotifyService) {
        this.service = service;
        this.spotifyService = spotifyService;
        this.monitoredSessions = new HashMap<>();
    }

    public HashMap<SessionDAO, String> getMonitoredSessions() {
        return monitoredSessions;
    }

    public void removeMonitoredSession(String id) {
        monitoredSessions.entrySet().removeIf(sessionStringEntry -> sessionStringEntry.getKey().getId().equals(id));
    }

    @Scheduled(fixedRate = 5000)
    private void checkPlaybackStatus() {
        for(Iterator<Map.Entry<SessionDAO, String>> it = monitoredSessions.entrySet().iterator(); it.hasNext();) {
            final Map.Entry<SessionDAO, String> entry = it.next();
            final TrackDAO track = spotifyService.getPlaybackStatus(entry.getKey().getOwner().getAccessToken());
            if(track != null) {
                final String trackId = entry.getValue();
                if(trackId != null) {
                    // Update session and alert all user about next track
                    service.getById(entry.getKey().getId())
                        .subscribe(update -> {
                            service.removeTrackById(update, trackId);
                            update.getVotes().removeIf(vote -> vote.getTrackId().equals(trackId));
                            update.getPlayedTracks().add(update.getCurrentTrack());
                            update.setCurrentTrack(track);
                            service.save(update).subscribe();
                        });
                    service.sendTrackStartEvent(
                        entry.getKey().getId(),
                        spotifyService.currentTrackDTOMapper(track)
                    );
                    entry.getKey().setCurrentTrack(track);
                    monitoredSessions.put(entry.getKey(), null);
                } else {
                    Long timestamp2 = track.getTimestamp() - track.getProgressMs();
                    Long timestamp1 = entry.getKey().getCurrentTrack().getTimestamp() -
                                      entry.getKey().getCurrentTrack().getProgressMs();
                    System.out.println(">>> td: " + (timestamp2-timestamp1) + " <<<");
                    if(timestamp2 - timestamp1 < 2000 && timestamp2 - timestamp1 > -500 &&
                    entry.getKey().getCurrentTrack().getTrackInfos().getId().equals(track.getTrackInfos().getId())) {
                        if(track.getTrackInfos().getTimeMs() - track.getProgressMs() < 5000) {
                            service.sendVoteStopEvent(entry.getKey().getId());
                            // Selects next track for this session
                            service.getById(entry.getKey().getId())
                                .subscribe(update -> {
                                    String nextTrackId = service.evaluateNextTrack(update);
                                    if(nextTrackId == null) {
                                        nextTrackId = service.selectRandomTrack(update);
                                    }
                                    spotifyService.addTrackToQueue(
                                        update.getOwner().getAccessToken(),
                                        update.getDeviceId(),
                                        nextTrackId
                                    );
                                    service.distributeNewVotes(update);
                                    monitoredSessions.put(update, nextTrackId);
                                    monitoredSessions.remove(entry.getKey());
                                });
                        }
                    } else {
                        // Owner controls spotify outside of this app
                        long timeMs = track.getTimestamp() - timestamp1;
                        service.sendSessionStopEvent(entry.getKey(), (int)timeMs);
                        it.remove();
                    }
                }
            } else {
                // wip: Handle playback stopped, changed device, toke expired
                service.closeSession(entry.getKey());
                it.remove();
            }
        }
    }
}
