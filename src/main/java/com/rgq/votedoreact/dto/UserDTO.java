package com.rgq.votedoreact.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserDTO {
    private String id;
    private String sessionId;
    private String username;
    private String imgUrl;
    private Integer votes;
}
