package com.rgq.votedoreact.sse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserSSE {
    @Id
    private String id;
    private Boolean status;
    private String message;
}
