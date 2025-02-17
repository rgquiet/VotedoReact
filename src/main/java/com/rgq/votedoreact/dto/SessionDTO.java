package com.rgq.votedoreact.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SessionDTO {
    private String id;
    private String name;
    private Boolean open;
    private String owner;
    private String ownerImg;
    private Integer members;
}
