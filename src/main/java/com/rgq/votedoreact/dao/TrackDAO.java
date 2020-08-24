package com.rgq.votedoreact.dao;

import com.rgq.votedoreact.dto.TrackDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrackDAO {
    private TrackDTO trackInfos;
    private Integer progressMs;
    private Long timestamp;
}
