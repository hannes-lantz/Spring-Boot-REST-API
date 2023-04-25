package com.example.mashup.services.clients;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import reactor.test.StepVerifier;


public class WikiDataClientTest {

    private MockWebServer mockWebServer;
    private WikiDataClient wikiDataClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        wikiDataClient = new WikiDataClient(WebClient.create(mockWebServer.url("/").toString()));
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testGetData() throws FileNotFoundException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File jsonFile = new File("src/test/resources/wikiData.json");
        JsonNode jsonNode = objectMapper.readTree(new FileInputStream(jsonFile));

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonNode.toString())
                .addHeader("Content-Type", "application/json"));

        StepVerifier.create(wikiDataClient.getData("Q11649"))
            .assertNext(response -> {
                assertEquals("Q11649", response.at("/entities/Q11649/id").asText());
            }).verifyComplete();

    }

    @Test
    void testGetWikiPageID() {

    }

    @Test
    void testGetData_NotFound() throws FileNotFoundException, IOException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404));

                StepVerifier.create(wikiDataClient.getData("PageNotFound"))
                    .verifyComplete();
    }
}
