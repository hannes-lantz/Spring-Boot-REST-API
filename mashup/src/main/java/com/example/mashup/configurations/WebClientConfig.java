package com.example.mashup.configurations;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient musicBrainzWebClient(@Value("${musicbrainzBaseUrl}") String baseUrl) {
        return configureWebClient(baseUrl);
    }

    @Bean
    public WebClient wikidataWebClient(@Value("${wikidataBaseUrl}") String baseUrl) {
        return configureWebClient(baseUrl);
    }

    @Bean
    public WebClient wikipediaWebClient(@Value("${wikipediaBaseUrl}") String baseUrl) {
        return configureWebClient(baseUrl);
    }

    @Bean
    public WebClient coverArtArchiveWebClient(@Value("${coverartarchiveBaseUrl}") String baseUrl) {
        return configureWebClient(baseUrl);
    }

    private WebClient configureWebClient(String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create()
                                .compress(true)
                                .responseTimeout(Duration.ofMillis(5000))
                                .followRedirect(true)))
                .build();
    }

}
