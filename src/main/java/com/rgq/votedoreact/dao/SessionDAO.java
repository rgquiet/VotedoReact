package com.rgq.votedoreact.dao;

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
public class SessionDAO {
    @Id
    private String id;
    private String deviceId;
    private String name;
    private Boolean open;
    @DBRef
    private UserDAO owner;
    @DBRef
    private List<UserDAO> members;
    private TrackDAO currentTrack;
    private List<TrackDAO> playedTracks;
    private List<VoteDAO> votes;
}
