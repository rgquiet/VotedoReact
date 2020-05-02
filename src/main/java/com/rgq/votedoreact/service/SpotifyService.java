package com.rgq.votedoreact.service;

import com.rgq.votedoreact.config.SpotifyAuthConfig;
import org.springframework.stereotype.Service;

@Service
public class SpotifyService {
    private String url;

    public SpotifyService(SpotifyAuthConfig config) {
        this.url = config.authUrl();
    }

    public String getUrl() {
        return url;
    }
}
