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
@Cacheable("wikidata")
public class WikiDataClient {
    private static Logger LOGGER = LoggerFactory.getLogger(MusicBrainzClient.class);

    @Autowired
    @Qualifier("wikidataWebClient")
    private final WebClient client;

    @Autowired
    public WikiDataClient(@Qualifier("wikidataWebClient") WebClient webClient) {
        this.client = webClient;
    }

    /**
     * Retrieves the Wikipedia page ID for a Wikidata entity.
     *
     * @param id The ID of the Wikidata entity to retrieve the Wikipedia page ID for.
     * 
     * @return An {@link Mono} containing the Wikipedia page ID as a String,
     *          or an empty Mono if the page was not found.
     */
    public Mono<String> getWikipediaTitle(String id) {
        return getData(id).map(response -> 
            response.at("/entities/" + id + "/sitelinks/enwiki/title").asText());
    }

    /**
     * Retrieves data for a Wikidata entity.
     *
     * @param id The ID of the Wikidata entity to retrieve data for.
     * 
     * @return An {@link Mono} containing the data for the Wikidata entity as a {@link JsonNode}.
     *         If the entity was not found, the Mono will be empty.
     */  
    public Mono<JsonNode> getData(String id) {
        LOGGER.info("Getting data from wikidata.org");

        return client.get()
                .uri(uriBuilder -> uriBuilder.path("/w/api.php")
                        .queryParam("action", "wbgetentities")
                        .queryParam("ids", id)
                        .queryParam("format", "json")
                        .queryParam("props", "sitelinks")
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .onErrorResume(WebClientResponseException.class, error -> error.getStatusCode().is4xxClientError() ? Mono.empty() : Mono.error(error));
    }
    
}
