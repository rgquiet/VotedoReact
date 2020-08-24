package com.rgq.votedoreact.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VoteDAO {
    private UUID id;
    private String userId;
    private String trackId;
}
