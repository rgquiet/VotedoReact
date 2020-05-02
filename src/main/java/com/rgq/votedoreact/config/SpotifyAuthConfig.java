package com.rgq.votedoreact.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpotifyAuthConfig {
    @Value("${spotify.auth.endpoint}")
    private String endpoint;
    @Value("${spotify.auth.clientId}")
    private String clientId;
    @Value("${spotify.auth.redirectUri}")
    private String redirectUri;
    @Value("${spotify.auth.scopes}")
    private String scopes;

    public String authUrl() {
        return endpoint
            + "?client_id=" + clientId
            + "&redirect_uri=" + redirectUri
            + "&scope=" + scopes
            + "&response_type=token&show_dialog=true";
    }
}
