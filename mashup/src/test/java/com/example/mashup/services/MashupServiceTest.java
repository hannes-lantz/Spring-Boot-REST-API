package com.example.mashup.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.example.mashup.datamodels.Album;
import com.example.mashup.datamodels.ArtistBand;
import com.example.mashup.services.clients.CoverArtArchiveClient;
import com.example.mashup.services.clients.MusicBrainzClient;
import com.example.mashup.services.clients.WikiDataClient;
import com.example.mashup.services.clients.WikipediaClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class MashupServiceTest {

    @Mock
    private MusicBrainzClient musicbrainz;

    @Mock
    private WikipediaClient wikipedia;

    @Mock
    private WikiDataClient wikiData;

    @Mock
    private CoverArtArchiveClient coverArt;

    private MashupService mashupService;

    @BeforeEach
    public void setUp() {
       mashupService = new MashupService(musicbrainz, wikipedia, wikiData, coverArt);
    }

    @Test
    public void testGetArtistBand() {
        String mbid = "5b11f4ce-a62d-471e-81fc-a69a8278c7da";
        // Set up mock responses
        when(musicbrainz.getArtist(mbid)).thenReturn(createMockArtistResponse());
        when(wikipedia.getDescription("wikipedia_title")).thenReturn(createMockWikipediaDescription());
        when(coverArt.getAlbums(createMockReleaseGroups())).thenReturn(createMockAlbums());

        // Invoke the service method
        Mono<ArtistBand> artistBand = mashupService.getArtistBand(mbid);

        // Verify the results
        StepVerifier.create(artistBand)
            .assertNext(musicData -> {
                assertNotNull(artistBand);
                assertEquals(mbid, musicData.getMbid());
                assertEquals("<p>This is a mock Wikipedia description</p>", musicData.getDescription());
                assertEquals(2, musicData.getAlbums().size());
                assertEquals("Album 1", musicData.getAlbums().get(0).getTitle());
                assertEquals("Album 2", musicData.getAlbums().get(1).getTitle());
            }).verifyComplete();

    }

    private Mono<JsonNode> createMockArtistResponse() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();

        ArrayNode releaseGroups = mapper.createArrayNode();
        ObjectNode releaseGroupContent1 = mapper.createObjectNode();
        ObjectNode releaseGroupContent2 = mapper.createObjectNode();

        releaseGroupContent1.put("title", "Album 1");
        releaseGroupContent2.put("title", "Album 2");
        releaseGroups.add(releaseGroupContent1);
        releaseGroups.add(releaseGroupContent2);

        ArrayNode relations = mapper.createArrayNode();
        ObjectNode relationContent = mapper.createObjectNode();

        relationContent.put("resource", "https://wikipedia.org/wiki/wikipedia_title");
        relations.add(relationContent);

        root.set("release-groups", releaseGroups);
        root.set("relations", relations);
        return Mono.just(root);
    }

    private Mono<String> createMockWikipediaDescription() {
        return Mono.just("<p>This is a mock Wikipedia description</p>");
    }

    private List<JsonNode> createMockReleaseGroups() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode releaseGroup1 = mapper.createObjectNode();
        ObjectNode releaseGroup2 = mapper.createObjectNode();
        releaseGroup1.put("title", "Album 1");
        releaseGroup2.put("title", "Album 2");
        return Arrays.asList(releaseGroup1, releaseGroup2);
    }

    private Mono<List<Album>> createMockAlbums() {
        List<Album> albums = Arrays.asList(
            new Album("1", "Album 1", "cover1.jpg"),
            new Album("2", "Album 2", "cover2.jpg")
        );
        return Mono.just(albums);
    }

    @Test
    public void testGetArtistBand_InvalidMBID_ShouldThrowException() {
        // set up mock response
        when(musicbrainz.getArtist("invalid")).thenReturn(Mono.empty());

        // Invoke the service method
        Mono<ArtistBand> artistBand = mashupService.getArtistBand("invalid");
        
        // Verify the results
        StepVerifier.create(artistBand)
            .expectError();
    }
}
