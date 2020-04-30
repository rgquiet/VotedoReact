package com.rgq.votedoreact.config;

import com.rgq.votedoreact.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestUserConfig {
    @Value("${test.user.name}")
    private String name;
    @Value("${test.user.email}")
    private String email;
    @Value("${test.user.password}")
    private String password;

    public User createTestUser() {
        return new User("testUser", name, email, password);
    }
}
