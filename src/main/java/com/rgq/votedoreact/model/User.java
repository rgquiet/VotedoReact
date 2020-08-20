package com.rgq.votedoreact.model;

import com.wrapper.spotify.enums.ProductType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {
    @Id
    private String id;
    private ProductType product;
    private String sessionId;
    private String trackId;
    private String username;
    private String email;
    private String imgUrl;
    private String accessToken;
    private Date expiresAt;
    private List<String> friends;
    private Integer votes;
}
