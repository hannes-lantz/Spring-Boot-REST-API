package com.example.mashup.services.clients;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.example.mashup.datamodels.Album;
import com.fasterxml.jackson.databind.JsonNode;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Cacheable("coverart")
public class CoverArtArchiveClient {
    private static Logger LOGGER = LoggerFactory.getLogger(CoverArtArchiveClient.class);

    @Autowired
    @Qualifier("coverArtArchiveWebClient")
    private final WebClient client;

    @Autowired
    public CoverArtArchiveClient(@Qualifier("coverArtArchiveWebClient") WebClient webClient) {
        this.client = webClient;
    }

    /**
     * Retrieves a list of Albums.
     *
     * @param releaseGroupList A list that contains info about albums.
     * 
     * @return A list of Album objects with the id, title, and album cover art
     *         included.
     */
    public Mono<List<Album>> getAlbums(List<JsonNode> releaseGroupList) {
        return Flux.fromIterable(releaseGroupList)
                .filter(album -> album.get("primary-type") != null)
                .flatMap(album -> {
                    String id = album.get("id").asText();
                    String title = album.get("title").asText();
                    return getCoverArt(id).map(response -> {
                        String coverArt = response.findPath("image").asText();
                        return new Album(id, title, coverArt);
                    }).switchIfEmpty(Mono.just(new Album(id, title, "No cover art found")));
                })
                .collectList().cache();
    }


    private Mono<JsonNode> getCoverArt(String id) {
        LOGGER.info("Getting data from coverartarchive.org");
        return client.get()
                .uri("/release-group/" + id)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .onErrorResume(WebClientResponseException.class, error -> error.getStatusCode().is4xxClientError() ? Mono.empty() : Mono.error(error));
    }

}
