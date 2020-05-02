package com.rgq.votedoreact.controller;

import com.rgq.votedoreact.dto.AccessDTO;
import com.rgq.votedoreact.dto.UserDTO;
import com.rgq.votedoreact.service.SpotifyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/spotify")
public class SpotifyController {
    private SpotifyService service;

    public SpotifyController(SpotifyService service) {
        this.service = service;
    }

    @GetMapping("/auth")
    public ResponseEntity<String> getAuth() {
        return ResponseEntity.ok(service.getUrl());
    }

    @PostMapping("/token")
    public ResponseEntity<?> getToken(@RequestBody AccessDTO accessDTO) {
        // wip: Set access token in spotify service
        if(accessDTO.getAccessToken().equals("asdf")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid Token");
        }
        return ResponseEntity.ok(new UserDTO("rg_quiet"));
    }
}
