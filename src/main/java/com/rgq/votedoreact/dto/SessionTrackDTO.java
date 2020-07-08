package com.rgq.votedoreact.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SessionTrackDTO {
    private String id;
    private String userId;
    private String name;
    private String artist;
    private String imgUrl;
    private Integer timeMs;
    private Integer votes;
}
