package com.rgq.votedoreact;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class VotedoReactApplication {

    public static void main(String[] args) {
        SpringApplication.run(VotedoReactApplication.class, args);
    }
}
