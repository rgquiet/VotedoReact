package com.rgq.votedoreact.service;

import com.rgq.votedoreact.config.SpotifyAuthConfig;
import com.rgq.votedoreact.dto.AccessDTO;
import com.rgq.votedoreact.dto.TrackDTO;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.model_objects.specification.User;
import com.wrapper.spotify.requests.data.search.simplified.SearchTracksRequest;
import com.wrapper.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;
import org.apache.hc.core5.http.ParseException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

@Service
public class SpotifyService {
    private final Logger logger = LoggerFactory.getLogger(SpotifyService.class);
    private final SpotifyApi apiPublic;
    private final String url;

    public SpotifyService(SpotifyAuthConfig config) {
        this.apiPublic = new SpotifyApi.Builder()
            .setClientId(config.getClientId())
            .setClientSecret(config.getClientSecret())
            .build();
        try {
            ClientCredentials credentials = apiPublic.clientCredentials().build().execute();
            apiPublic.setAccessToken(credentials.getAccessToken());
        } catch(IOException e) {
            logger.error("{}", e);
        } catch(SpotifyWebApiException e) {
            logger.error("{}", e);
        } catch(ParseException e) {
            logger.error("{}", e);
        }
        this.url = config.authUrl();
    }

    public String getUrl() {
        return url;
    }

    public com.rgq.votedoreact.model.User getSpotifyUser(AccessDTO dto) {
        SpotifyApi api = new SpotifyApi.Builder().setAccessToken(dto.getAccessToken()).build();
        GetCurrentUsersProfileRequest userRequest = api.getCurrentUsersProfile().build();
        try {
            final CompletableFuture<User> future = userRequest.executeAsync();
            return future.thenApply(userProfile -> {
                com.rgq.votedoreact.model.User user = new com.rgq.votedoreact.model.User(
                    userProfile.getId(),
                    null,
                    null,
                    userProfile.getDisplayName(),
                    userProfile.getEmail(),
                    // wip: Random N. Cage image
                    "https://image.gala.de/22033424/3x2-940-627/4d877474228396b51c0d7d71679edd9e/fN/nicolas-cage.jpg",
                    dto.getAccessToken(),
                    new Date(dto.getTimestamp()),
                    new ArrayList<>()
                );
                if(userProfile.getImages().length > 0) {
                    user.setImgUrl(userProfile.getImages()[0].getUrl());
                }
                return user;
            }).get();
        } catch(InterruptedException e) {
            logger.error("{}", e);
        } catch(ExecutionException e) {
            logger.error("{}", e);
        } catch(CompletionException e) {
            logger.error("{}", e);
        } catch(CancellationException e) {
            logger.info("{}", "Async operation cancelled.");
        }
        return null;
    }

    public List<TrackDTO> searchSpotifyTrack(String name) {
        SearchTracksRequest trackRequest = apiPublic.searchTracks(name).limit(10).build();
        try {
            final CompletableFuture<Paging<Track>> pagingFuture = trackRequest.executeAsync();
            return pagingFuture.thenApply(result -> {
                List<TrackDTO> trackDTOs = new ArrayList<>();
                Track[] tracks = result.getItems();
                for(int i = 0; i < tracks.length; i++) {
                    String artists = new String();
                    for(int j = 0; j < tracks[i].getArtists().length; j++) {
                        artists = artists + tracks[i].getArtists()[j].getName() + ", ";
                    }
                    artists = artists.substring(0, artists.length() - 2);
                    trackDTOs.add(new TrackDTO(
                        tracks[i].getId(),
                        tracks[i].getName(),
                        artists,
                        tracks[i].getAlbum().getImages()[0].getUrl(),
                        tracks[i].getDurationMs()
                    ));
                }
                return trackDTOs;
            }).get();
        } catch(InterruptedException e) {
            logger.error("{}", e);
        } catch(ExecutionException e) {
            logger.error("{}", e);
        } catch(CompletionException e) {
            logger.error("{}", e);
        } catch(CancellationException e) {
            logger.info("{}", "Async operation cancelled.");
        }
        return null;
    }
}
