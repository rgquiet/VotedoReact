package com.rgq.votedoreact.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ExistDTO {
    private boolean exist;

    // wip: Why @Getter doesn't work?
    public boolean getExist() {
        return exist;
    }
}
