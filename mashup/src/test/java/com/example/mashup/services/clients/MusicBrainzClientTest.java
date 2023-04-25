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


public class MusicBrainzClientTest {

    private MockWebServer mockWebServer;
    private MusicBrainzClient musicBrainzClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        musicBrainzClient = new MusicBrainzClient(WebClient.create(mockWebServer.url("/").toString()));
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }


    @Test
    void testGetArtist() throws IOException, InterruptedException {
        ObjectMapper objectMapper = new ObjectMapper();
        File jsonFile = new File("src/test/resources/artist.json");
        JsonNode jsonNode = objectMapper.readTree(new FileInputStream(jsonFile));

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonNode.toString())
                .addHeader("Content-Type", "application/json"));

        StepVerifier.create(musicBrainzClient.getArtist("056e4f3e-d505-4dad-8ec1-d04f521cbb56"))
                .assertNext(artist -> {
                    assertEquals("Daft Punk", artist.get("sort-name").textValue());
                    assertEquals("056e4f3e-d505-4dad-8ec1-d04f521cbb56", artist.get("id").textValue());
                })
                .verifyComplete();
    }

    @Test
    void testGetDescription_NotFound() throws FileNotFoundException, IOException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404));

                StepVerifier.create(musicBrainzClient.getArtist("PageNotFound"))
                    .verifyComplete();
    
    }

}