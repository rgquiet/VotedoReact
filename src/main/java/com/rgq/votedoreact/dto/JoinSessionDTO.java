package com.rgq.votedoreact.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class JoinSessionDTO {
    private String id;
    private String name;
    private Integer votes;
    private CurrentTrackDTO track;
}
