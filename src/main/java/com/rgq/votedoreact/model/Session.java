package com.rgq.votedoreact.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "sessions")
public class Session {
    @Id
    private String id;
    private String name;
    @DBRef
    private User owner;
}
