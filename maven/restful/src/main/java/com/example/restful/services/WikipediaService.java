package com.example.restful.services;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;



@Service
@Cacheable("wiki-description")
@ConfigurationProperties("wikipedia")
public class WikipediaService {

    private static final String API_PATH = "/w/api.php?action=query&format=json&prop=extracts&exintro=true&redirects=true&titles={title}";

    @Autowired
  	RestTemplate api;
   
    private String baseURI;

    private static Logger LOGGER = LoggerFactory.getLogger(WikipediaService.class);

    public String getBaseURI() {
        return baseURI;
    }

    public void setBaseURI(String baseURI) {
        this.baseURI = baseURI;

    }

    public Optional<String> getDescription(String title) {

        LOGGER.debug("baseURI: {}", getBaseURI());
        try {
            LOGGER.info("Getting data from Wikipedia");
            JsonNode response = api.getForObject(getBaseURI().concat(API_PATH), JsonNode.class, title);

            if (response != null) {
                List<String> extracts = response.findValuesAsText("extract");
                if (!extracts.isEmpty()) {
                    String description = extracts.get(0);

                    //makes the description output a bit nicer
                    int start = description.indexOf("<p><b>");
                    int end = description.lastIndexOf("</p>")+4;

                    return Optional.ofNullable(description.substring(start, end));
                }
            }
            return null;

        } catch (RestClientException e) {
            LOGGER.debug(e.getMessage());
            return null;
        }
    }
}