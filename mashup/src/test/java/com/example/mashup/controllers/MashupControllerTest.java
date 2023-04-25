package com.example.mashup.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.mashup.datamodels.Album;
import com.example.mashup.datamodels.ArtistBand;
import com.example.mashup.services.MashupService;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


@ExtendWith(MockitoExtension.class)
public class MashupControllerTest {


    @Mock
    private MashupService mashupService;

    String MBID = "5b11f4ce-a62d-471e-81fc-a69a8278c7da";


    @Test
    void testGetMashup_Existing_MBID() {
        // Set up mock responses
        Mockito.when(mashupService.getArtistBand(MBID)).thenReturn(createMockArtist());

        // Invoke the service method
        Mono<ArtistBand> artistBand = mashupService.getArtistBand(MBID);

        // Verify the results
        StepVerifier.create(artistBand)
            .assertNext(musicData -> {
                assertNotNull(artistBand);
                assertEquals(MBID, musicData.getMbid());
                assertEquals("<p>This is a mock Wikipedia description</p>", musicData.getDescription());
                assertEquals(1, musicData.getAlbums().size());
                assertEquals("Album 1", musicData.getAlbums().get(0).getTitle());
            }).verifyComplete();
            
    }


    private Mono<ArtistBand> createMockArtist() {
        ArtistBand artist = new ArtistBand();
        artist.setMbid(MBID);
        artist.setDescription("<p>This is a mock Wikipedia description</p>");

        List<Album> albums = new ArrayList<>();
        albums.add(new Album("1111", "Album 1", "coverart"));

        artist.setAlbums(albums);
        return Mono.just(artist);
    }

}
