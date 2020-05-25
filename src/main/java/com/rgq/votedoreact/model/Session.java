package com.rgq.votedoreact.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "sessions")
public class Session {
    @Id
    private String id;
    private String name;
    private Boolean open;
    @DBRef
    private User owner;
    @DBRef
    private List<User> members;
}
