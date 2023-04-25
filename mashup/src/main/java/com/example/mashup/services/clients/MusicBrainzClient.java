package com.example.mashup.services.clients;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;

import reactor.core.publisher.Mono;

@Service
@Cacheable("musicbrainz")
public class MusicBrainzClient {
    private static Logger LOGGER = LoggerFactory.getLogger(MusicBrainzClient.class);

    @Autowired
    @Qualifier("musicBrainzWebClient")
    private final WebClient client;

    @Autowired
    public MusicBrainzClient(@Qualifier("musicBrainzWebClient") WebClient webClient) {
        this.client = webClient;
    }


    /**
     * Retrieves data for an artist from MusicBrainz.
     *
     * @param mbid The MusicBrainz ID of the artist to retrieve data for.
     * 
     * @return An {@link Mono} containing the data for the artist as a {@link JsonNode}.
     *         If the artist was not found, the Mono will be empty.
     */
    public Mono<JsonNode> getArtist(String mbid) {
        LOGGER.info("Getting data from musicbrainz.org");

        return client.get()
                .uri(uriBuilder -> uriBuilder.path("/artist/{mbid}")
                        .queryParam("fmt", "json")
                        .queryParam("inc", "url-rels+release-groups")
                        .build(mbid))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .onErrorResume(WebClientResponseException.class, error -> error.getStatusCode().is4xxClientError() ? Mono.empty() : Mono.error(error));
    }

}
