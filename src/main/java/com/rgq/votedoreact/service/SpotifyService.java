package com.rgq.votedoreact.service;

import com.rgq.votedoreact.config.SpotifyAuthConfig;
import com.rgq.votedoreact.dto.AccessDTO;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.model_objects.specification.User;
import com.wrapper.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.*;

@Service
public class SpotifyService {
    private final Logger logger = LoggerFactory.getLogger(SpotifyService.class);
    private String url;

    public SpotifyService(SpotifyAuthConfig config) {
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
                    userProfile.getDisplayName(),
                    userProfile.getEmail(),
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
}
