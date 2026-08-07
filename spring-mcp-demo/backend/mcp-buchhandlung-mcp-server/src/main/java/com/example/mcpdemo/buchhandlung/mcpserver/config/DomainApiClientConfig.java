package com.example.mcpdemo.buchhandlung.mcpserver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class DomainApiClientConfig {

    @Bean
    public RestClient domainApiRestClient(@Value("${buchhandlung.domain-api.base-url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }
}
