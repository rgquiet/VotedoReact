package com.rgq.votedoreact.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateSessionDTO {
    private String userId;
    private String name;
    // wip...
    private String[] invitations;
}
