package com.rgq.votedoreact.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class StopTrackDTO {
    private String sessionName;
    private String ownerId;
    private String trackId;
    private Integer progressMs;
}
