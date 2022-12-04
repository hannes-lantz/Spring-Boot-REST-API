package com.example.restful.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;



@Service
@Cacheable("wikidata")
@ConfigurationProperties("wikidata")
public class WikiDataService {
    
    private static final String API_PATH = "/w/api.php?action=wbgetentities&ids={id}&format=json&props=sitelinks";

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

    public String  getWikiPage(String id){
        
        LOGGER.debug("baseURI: {}", getBaseURI());
        try {
            LOGGER.info("Getting data from WikiData");
            JsonNode response = api.getForObject(getBaseURI().concat(API_PATH), JsonNode.class, id);
            
            if(response != null){
                String title = response.at("/entities/"+id+"/sitelinks/enwiki/title").asText();
                return title;
			}
			return null;
        } catch (RestClientException e) {
            LOGGER.debug(e.getMessage());
            return null;
        }
    }
}
