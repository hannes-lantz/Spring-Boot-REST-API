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
@ConfigurationProperties("cover-art-archive")
public class CoverArtService {
	private static final String API_PATH = "/release-group/{id}";

	@Autowired
  	RestTemplate api;

	private static Logger LOGGER = LoggerFactory.getLogger(CoverArtService.class);

	private String baseURI;

	public String getBaseURI() {
		return baseURI;
	}

	public void setBaseURI(String baseURI) {
		this.baseURI = baseURI;
	}

	
	@Cacheable("album-covers")
	public String getCoverArt(String id) {

		LOGGER.debug("baseURI: {}", getBaseURI());
		String imagePath = null;
		try {
			LOGGER.info("Getting data from Cover Art Archive");
			JsonNode response = api.getForObject(getBaseURI().concat(API_PATH), JsonNode.class, id);
			if(response != null){
				imagePath = response.findPath("image").asText();
			}

		} catch (RestClientException e) {
			LOGGER.debug(e.getMessage());
		}

		
		return imagePath;
	}

}