package com.example.mashup.services.clients;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.mashup.datamodels.Album;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.test.StepVerifier;

public class CoverArtArchiveClientTest {

    private MockWebServer mockWebServer;
    private CoverArtArchiveClient coverArtClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        coverArtClient = new CoverArtArchiveClient(WebClient.builder().baseUrl(mockWebServer.url("/").toString())
            .clientConnector(new ReactorClientHttpConnector(
                HttpClient.create()
                    .compress(true)
                    .responseTimeout(Duration.ofMillis(50))
                    .followRedirect(true)))
            .build()
        );
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testGetAlbums() throws FileNotFoundException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File jsonFile = new File("src/test/resources/coverArt.json");
        JsonNode jsonNode = objectMapper.readTree(new FileInputStream(jsonFile));

        // First response with a redirect location
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Location", "/download/mbid-a146429a-cedc-3ab0-9e41-1aaf5f6cdc2d/index.json")
                .setResponseCode(307));
        // Second response with a redirect location
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Location", "/11/items/mbid-a146429a-cedc-3ab0-9e41-1aaf5f6cdc2d/index.json")
                .setResponseCode(302));
        //Final response with the album data
        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonNode.toString())
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json"));

        Album expected = createMockAlbum();

        Mono<List<Album>> albumFlux = coverArtClient.getAlbums(createMockReleaseGroups());

        StepVerifier.create(albumFlux)
                .assertNext(response -> {
                    assertEquals(expected.getId(), response.get(0).getId());
                    assertEquals(expected.getTitle(), response.get(0).getTitle());
                    assertEquals(expected.getCoverArt(), response.get(0).getCoverArt());
                })
                .verifyComplete();

    }

    private Album createMockAlbum() {
        Album album = new Album(
                "1b022e01-4da6-387b-8658-8678046e4cef",
                "Nevermind",
                "http://coverartarchive.org/release/a146429a-cedc-3ab0-9e41-1aaf5f6cdc2d/3012495605.jpg");
        return album;
    }

    private List<JsonNode> createMockReleaseGroups() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode releaseGroup = mapper.createObjectNode();
        releaseGroup.put("id", "1b022e01-4da6-387b-8658-8678046e4cef");
        releaseGroup.put("title", "Nevermind");
        releaseGroup.put("primary-type", "Album");
        return Arrays.asList(releaseGroup);
    }

    @Test
    void testGetAlbums_NotFound() throws FileNotFoundException, IOException {
        // First response with status 404
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404));

        Album expected = createMockAlbum();

        StepVerifier.create(coverArtClient.getAlbums(createMockReleaseGroups()))
                .assertNext(response -> {
                    assertEquals(expected.getId(), response.get(0).getId());
                    assertEquals(expected.getTitle(), response.get(0).getTitle());
                    assertEquals("No cover art found", response.get(0).getCoverArt());
                })
                .verifyComplete();

    }

    @Test
    void testGetAlbums_NotFound_redirect1() throws FileNotFoundException, IOException {
        // First response with a redirect location
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Location", "/download/mbid-a146429a-cedc-3ab0-9e41-1aaf5f6cdc2d/index.json")
                .setResponseCode(307));

        // Second response with status 404
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Location", "/11/items/mbid-a146429a-cedc-3ab0-9e41-1aaf5f6cdc2d/index.json")
                .setResponseCode(404));

        Album expected = createMockAlbum();

        StepVerifier.create(coverArtClient.getAlbums(createMockReleaseGroups()))
                .assertNext(response -> {
                    assertEquals(expected.getId(), response.get(0).getId());
                    assertEquals(expected.getTitle(), response.get(0).getTitle());
                    assertEquals("No cover art found", response.get(0).getCoverArt());
                })
                .verifyComplete();

    }

    @Test
    void testGetAlbums_NotFound_redirect2() throws FileNotFoundException, IOException {
        // First response with a redirect location
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Location", "/download/mbid-a146429a-cedc-3ab0-9e41-1aaf5f6cdc2d/index.json")
                .setResponseCode(307));
        // Second response with a redirect location
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Location", "/11/items/mbid-a146429a-cedc-3ab0-9e41-1aaf5f6cdc2d/index.json")
                .setResponseCode(302));

        // Final response with status 404
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404));
        
        Album expected = createMockAlbum();

        StepVerifier.create(coverArtClient.getAlbums(createMockReleaseGroups()))
                .assertNext(response -> {
                    assertEquals(expected.getId(), response.get(0).getId());
                    assertEquals(expected.getTitle(), response.get(0).getTitle());
                    assertEquals("No cover art found", response.get(0).getCoverArt());
                })
                .verifyComplete();

    }

    @Test
    void testGetAlbums_NoPrimarytype() throws FileNotFoundException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode releaseGroup = mapper.createObjectNode();
        releaseGroup.put("id", "1b022e01-4da6-387b-8658-8678046e4cef");
        releaseGroup.put("title", "Nevermind");

        StepVerifier.create(coverArtClient.getAlbums(Arrays.asList(releaseGroup)))
                .assertNext(response -> {
                    assertEquals(true, response.isEmpty());
        
                })
                .verifyComplete();

    }
}
