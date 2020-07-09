package com.rgq.votedoreact.service;

import com.rgq.votedoreact.config.SpotifyAuthConfig;
import com.rgq.votedoreact.dto.AccessDTO;
import com.rgq.votedoreact.dto.TrackDTO;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.miscellaneous.Device;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.model_objects.specification.User;
import com.wrapper.spotify.requests.data.player.GetUsersAvailableDevicesRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchTracksRequest;
import com.wrapper.spotify.requests.data.tracks.GetTrackRequest;
import com.wrapper.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;
import org.apache.hc.core5.http.ParseException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

@Service
public class SpotifyService {
    private final Logger logger = LoggerFactory.getLogger(SpotifyService.class);
    private final SpotifyApi apiPublic;
    private final String url;
    private final String[] cagesUrl = {
        "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c0/Nicolas_Cage_Deauville_2013.jpg/330px-Nicolas_Cage_Deauville_2013.jpg",
        "https://aisvip-a.akamaihd.net/masters/1290077/1920x1200/nicolas-cage-spielt-nicolas-cage-in-film-ueber-nicolas-cage.jpg",
        "https://image.gala.de/22033424/3x2-940-627/4d877474228396b51c0d7d71679edd9e/fN/nicolas-cage.jpg",
        "https://filmpluskritik.files.wordpress.com/2019/11/nick-cage-conair.jpg?w=570",
        "https://images-na.ssl-images-amazon.com/images/I/41TgiyaPvxL._AC_.jpg",
        "https://de.web.img3.acsta.net/pictures/15/07/20/18/14/582462.jpg",
        "https://assets.puzzlefactory.pl/puzzle/217/875/original.jpg"
    };

    public SpotifyService(SpotifyAuthConfig config) {
        this.apiPublic = new SpotifyApi.Builder()
            .setClientId(config.getClientId())
            .setClientSecret(config.getClientSecret())
            .build();
        refreshAccessToken();
        this.url = config.authUrl();
    }

    private void refreshAccessToken() {
        // wip: Return Completable and then retry failed request
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
                    cagesUrl[new Random().nextInt(cagesUrl.length - 1)],
                    dto.getAccessToken(),
                    new Date(dto.getTimestamp()),
                    new ArrayList<>(),
                    0
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

    public List<String> getAvailableDevices(String accessToken) {
        SpotifyApi api = new SpotifyApi.Builder().setAccessToken(accessToken).build();
        GetUsersAvailableDevicesRequest devicesRequest = api.getUsersAvailableDevices().build();
        try {
            final CompletableFuture<Device[]> future = devicesRequest.executeAsync();
            return future.thenApply(devices -> {
                List<String> deviceNames = new ArrayList<>();
                if(devices.length == 0) {
                    deviceNames.add("No devices available");
                } else {
                    for(int i = 0; i < devices.length; i++) {
                        deviceNames.add(devices[i].getName());
                    }
                }
                return deviceNames;
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

    public TrackDTO getTrackById(String id) {
        GetTrackRequest trackRequest = apiPublic.getTrack(id).build();
        try {
            final CompletableFuture<Track> future = trackRequest.executeAsync();
            return future.thenApply(result -> trackDTOMapper(result)).get();
        } catch(InterruptedException e) {
            logger.error("{}", e);
        } catch(ExecutionException e) {
            logger.error("{}", e);
            refreshAccessToken();
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
                List<TrackDTO> dtos = new ArrayList<>();
                Track[] tracks = result.getItems();
                for(int i = 0; i < tracks.length; i++) {
                    dtos.add(trackDTOMapper(tracks[i]));
                }
                return dtos;
            }).get();
        } catch(InterruptedException e) {
            logger.error("{}", e);
        } catch(ExecutionException e) {
            logger.error("{}", e);
            refreshAccessToken();
        } catch(CompletionException e) {
            logger.error("{}", e);
        } catch(CancellationException e) {
            logger.info("{}", "Async operation cancelled.");
        }
        return null;
    }

    private TrackDTO trackDTOMapper(Track track) {
        String artists = new String();
        for(int j = 0; j < track.getArtists().length; j++) {
            artists = artists + track.getArtists()[j].getName() + ", ";
        }
        artists = artists.substring(0, artists.length() - 2);
        return new TrackDTO(
            track.getId(),
            track.getName(),
            artists,
            track.getAlbum().getImages()[0].getUrl(),
            track.getDurationMs()
        );
    }
}
