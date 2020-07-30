package com.rgq.votedoreact.model;

import com.rgq.votedoreact.dto.TrackDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Track {
    private TrackDTO trackInfos;
    private Integer progressMs;
    private Long timestamp;
}
