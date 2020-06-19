package com.rgq.votedoreact.sse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SessionSSE {
    private EventType type;
    private Object dto;
}
