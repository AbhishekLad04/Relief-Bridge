package com.reliefbridge.volunteer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class VolunteerAIConfig {

    @Bean
    public RestClient volunteerAIRestClient() {

        return RestClient.builder()
                .baseUrl("http://localhost:8000")
                .build();
    }
}