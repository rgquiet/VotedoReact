package com.rgq.votedoreact.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "sessions")
public class Session {
    @Id
    private String id;
    private String deviceId;
    private String name;
    private Boolean open;
    @DBRef
    private User owner;
    @DBRef
    private List<User> members;
    private Track currentTrack;
    private List<Track> playedTracks;
    private List<Vote> votes;
}
