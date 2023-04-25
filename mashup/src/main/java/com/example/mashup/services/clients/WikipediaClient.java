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
@Cacheable("wikipedia")
public class WikipediaClient {
    private static Logger LOGGER = LoggerFactory.getLogger(MusicBrainzClient.class);

    @Autowired
    @Qualifier("wikipediaWebClient")
    private final WebClient client;

    @Autowired
    public WikipediaClient(@Qualifier("wikipediaWebClient") WebClient webClient) {
        this.client = webClient;
    }
   
    /**
     * Retrieves the description of a Wikipedia article.
     *
     * @param title The title of the Wikipedia article to retrieve the description for.
     * 
     * @return An {@link Mono} containing  the description as a String, if no exreact is found 
     *         it return "No description found" as a String,
     *         or an empty Mono if the article was not found.
     */
    public Mono<String> getDescription(String title) {
        LOGGER.info("Getting data from wikipedia.org");

        return client.get()
                .uri(uriBuilder -> uriBuilder.path("/w/api.php")
                        .queryParam("action", "query")
                        .queryParam("format", "json")
                        .queryParam("prop", "extracts")
                        .queryParam("exintro", true)
                        .queryParam("redirects", true)
                        .queryParam("titles", title)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .onErrorResume(WebClientResponseException.class, error -> error.getStatusCode().is4xxClientError() ? Mono.empty() : Mono.error(error))
                .map(json -> json.findValuesAsText("extract"))
                .map(extracts -> {
                    if (extracts.isEmpty()) {
                        return "No description found";
                    }
                    String extract = extracts.get(0);
                    int start = extract.indexOf("<p><b>"); // makes the description output a bit nicer
                    int end = extract.lastIndexOf("</p>") + 4;
                    return extract.substring(start, end);
                });
                    
    }
    
}
