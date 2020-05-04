package com.rgq.votedoreact.service;

import com.rgq.votedoreact.config.SpotifyAuthConfig;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.model_objects.specification.User;
import com.wrapper.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public com.rgq.votedoreact.model.User getSpotifyUser(String accessToken) {
        SpotifyApi api = new SpotifyApi.Builder().setAccessToken(accessToken).build();
        GetCurrentUsersProfileRequest userRequest = api.getCurrentUsersProfile().build();
        try {
            final CompletableFuture<User> future = userRequest.executeAsync();
            return future.thenApply(userProfile -> {
                if(userProfile.getImages().length > 0) {
                    return new com.rgq.votedoreact.model.User(
                        userProfile.getId(),
                        userProfile.getDisplayName(),
                        userProfile.getEmail(),
                        userProfile.getImages()[0].getUrl(),
                        accessToken
                    );
                } else {
                    return new com.rgq.votedoreact.model.User(
                        userProfile.getId(),
                        userProfile.getDisplayName(),
                        userProfile.getEmail(),
                        null,
                        accessToken
                    );
                }
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
