package com.rgq.votedoreact.service;

import com.rgq.votedoreact.config.SpotifyAuthConfig;
import com.rgq.votedoreact.dto.AccessDTO;
import com.rgq.votedoreact.dto.CurrentTrackDTO;
import com.rgq.votedoreact.dto.DeviceDTO;
import com.rgq.votedoreact.dto.TrackDTO;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.miscellaneous.CurrentlyPlayingContext;
import com.wrapper.spotify.model_objects.miscellaneous.Device;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.model_objects.specification.User;
import com.wrapper.spotify.requests.data.player.AddItemToUsersPlaybackQueueRequest;
import com.wrapper.spotify.requests.data.player.GetInformationAboutUsersCurrentPlaybackRequest;
import com.wrapper.spotify.requests.data.player.GetUsersAvailableDevicesRequest;
import com.wrapper.spotify.requests.data.player.StartResumeUsersPlaybackRequest;
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
        } catch(IOException | SpotifyWebApiException | ParseException e) {
            logger.error("Error occurred: ", e);
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
                // wip: Only premium users are allowed to create a session
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
        } catch(InterruptedException | ExecutionException | CompletionException e) {
            logger.error("Error occurred: ", e);
        } catch(CancellationException e) {
            logger.info("Async operation cancelled");
        }
        return null;
    }

    public List<DeviceDTO> getAvailableDevices(String accessToken) {
        SpotifyApi api = new SpotifyApi.Builder().setAccessToken(accessToken).build();
        GetUsersAvailableDevicesRequest devicesRequest = api.getUsersAvailableDevices().build();
        try {
            final CompletableFuture<Device[]> future = devicesRequest.executeAsync();
            return future.thenApply(devices -> {
                List<DeviceDTO> dtos = new ArrayList<>();
                if(devices.length == 0) {
                    dtos.add(new DeviceDTO(
                        "No devices available",
                        null
                    ));
                } else {
                    for(Device device : devices) {
                        dtos.add(new DeviceDTO(
                            device.getId(),
                            device.getName()
                        ));
                    }
                }
                return dtos;
            }).get();
        } catch(InterruptedException | ExecutionException | CompletionException e) {
            logger.error("Error occurred: ", e);
        } catch(CancellationException e) {
            logger.info("Async operation cancelled");
        }
        return null;
    }

    public com.rgq.votedoreact.model.Track getPlaybackStatus(String accessToken) {
        SpotifyApi api = new SpotifyApi.Builder().setAccessToken(accessToken).build();
        GetInformationAboutUsersCurrentPlaybackRequest playbackRequest =
        api.getInformationAboutUsersCurrentPlayback().build();
        final CompletableFuture<CurrentlyPlayingContext> future = playbackRequest.executeAsync();
        try {
            return future.thenApply(playback -> {
                if(playback.getItem() instanceof Track) {
                    Track spotifyTrack = (Track)playback.getItem();
                    return new com.rgq.votedoreact.model.Track(
                        trackDTOMapper(spotifyTrack),
                        playback.getProgress_ms(),
                        new Date().getTime()
                    );
                }
                return null;
            }).get();
        } catch(InterruptedException | ExecutionException | CompletionException e) {
            logger.error("Error occurred: ", e);
        } catch(CancellationException e) {
            logger.info("Async operation cancelled");
        }
        return null;
    }

    public TrackDTO getTrackById(String id) {
        GetTrackRequest trackRequest = apiPublic.getTrack(id).build();
        try {
            final CompletableFuture<Track> future = trackRequest.executeAsync();
            return future.thenApply(this::trackDTOMapper).get();
        } catch(ExecutionException e) {
            refreshAccessToken();
        } catch(InterruptedException | CompletionException e) {
            logger.error("Error occurred: ", e);
        } catch(CancellationException e) {
            logger.info("Async operation cancelled");
        }
        return null;
    }

    public List<TrackDTO> searchTrackByName(String name) {
        SearchTracksRequest trackRequest = apiPublic.searchTracks(name).limit(10).build();
        try {
            final CompletableFuture<Paging<Track>> pagingFuture = trackRequest.executeAsync();
            return pagingFuture.thenApply(result -> {
                List<TrackDTO> dtos = new ArrayList<>();
                Track[] tracks = result.getItems();
                for(Track track : tracks) {
                    dtos.add(trackDTOMapper(track));
                }
                return dtos;
            }).get();
        } catch(ExecutionException e) {
            refreshAccessToken();
        } catch(InterruptedException | CompletionException e) {
            logger.error("Error occurred: ", e);
        } catch(CancellationException e) {
            logger.info("Async operation cancelled");
        }
        return null;
    }

    public void startPlaybackOnDevice(String accessToken, String deviceId) {
        SpotifyApi api = new SpotifyApi.Builder().setAccessToken(accessToken).build();
        StartResumeUsersPlaybackRequest startPlaybackRequest = api
            .startResumeUsersPlayback()
            // Start playback with the default track 'Twisted Fate'
            .context_uri("spotify:album:1M6lrKUjfIiMpitc0xeh3x")
            .device_id(deviceId)
            .build();
        try {
            startPlaybackRequest.executeAsync().get();
        } catch(InterruptedException | ExecutionException e) {
            logger.error("Error occurred: ", e);
        }
    }

    public void addTrackToQueue(String accessToken, String deviceId, String trackId) {
        SpotifyApi api = new SpotifyApi.Builder().setAccessToken(accessToken).build();
        AddItemToUsersPlaybackQueueRequest queueRequest = api
            .addItemToUsersPlaybackQueue("spotify:track:" + trackId)
            .device_id(deviceId)
            .build();
        try {
            queueRequest.executeAsync().get();
        } catch(InterruptedException | ExecutionException e) {
            logger.error("Error occurred: ", e);
        }
    }

    private TrackDTO trackDTOMapper(Track track) {
        StringBuilder artists = new StringBuilder();
        for(int j = 0; j < track.getArtists().length; j++) {
            artists.append(track.getArtists()[j].getName()).append(", ");
        }
        artists = new StringBuilder(artists.substring(0, artists.length() - 2));
        return new TrackDTO(
            track.getId(),
            track.getName(),
            artists.toString(),
            track.getAlbum().getImages()[0].getUrl(),
            track.getDurationMs()
        );
    }

    public CurrentTrackDTO currentTrackDTOMapper(com.rgq.votedoreact.model.Track track) {
        return new CurrentTrackDTO(
            track.getTrackInfos().getId(),
            track.getTrackInfos().getName(),
            track.getTrackInfos().getArtist(),
            track.getTrackInfos().getImgUrl(),
            track.getTrackInfos().getTimeMs(),
            track.getTimestamp() - track.getProgressMs()
        );
    }
}
