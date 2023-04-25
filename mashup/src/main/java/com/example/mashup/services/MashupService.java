package com.example.mashup.services;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.mashup.datamodels.Album;
import com.example.mashup.datamodels.ArtistBand;
import com.example.mashup.services.clients.CoverArtArchiveClient;
import com.example.mashup.services.clients.MusicBrainzClient;
import com.example.mashup.services.clients.WikiDataClient;
import com.example.mashup.services.clients.WikipediaClient;
import com.fasterxml.jackson.databind.JsonNode;

import reactor.core.publisher.Mono;

@Service
@ConfigurationProperties("musicbrainz")
public class MashupService {

    private static Logger LOGGER = LoggerFactory.getLogger(CoverArtArchiveClient.class);

    private final MusicBrainzClient musicbrainz;
    private final WikipediaClient wikipedia;
    private final WikiDataClient wikiData;
    private final CoverArtArchiveClient coverArt;

    @Autowired
    public MashupService(MusicBrainzClient musicbrainz, WikipediaClient wikipedia, WikiDataClient wikiData,
            CoverArtArchiveClient coverArt) {
        this.musicbrainz = musicbrainz;
        this.wikipedia = wikipedia;
        this.wikiData = wikiData;
        this.coverArt = coverArt;
    }

    /**
     * Retrieves information about an artist or band, including a description, a
     * list of albums, and cover art for each album.
     *
     * @param mbid The MusicBrainz ID of the artist or band.
     * 
     * @return A {@link Mono}  of ArtistBand object containing information about the artist or band.
     * 
     * If mbid not found or invalid, it will return Mono.error(new ResponseStatusException(...))
     * 
     */
    @Cacheable("artistband")
    public Mono<ArtistBand> getArtistBand(String mbid) {

        return musicbrainz.getArtist(mbid).flatMap(musicData -> {
            LOGGER.debug("Got data from musicbrainz.org");

            List<JsonNode> releaseGroups = musicData.get("release-groups").findParents("title");
            List<String> relations = musicData.get("relations").findValuesAsText("resource");

            Mono<String> descriptionMono = Mono.justOrEmpty(findWikiTag(relations, "wikipedia"))
                    .flatMap(wikipediaId -> {
                        String wikiIdentifier = URLEncoder.encode(wikipediaId.replaceAll("[()]", ""),
                                StandardCharsets.UTF_8);
                        return wikipedia.getDescription(wikiIdentifier);
                    })
                    .switchIfEmpty(Mono.justOrEmpty(findWikiTag(relations, "wikidata"))
                            .flatMap(wikiDataId -> {
                                return wikiData.getWikipediaTitle(wikiDataId).flatMap(wikipediaTitle -> {
                                    String wikiIdentifier = URLEncoder.encode(wikipediaTitle.replaceAll("[()]", ""),
                                            StandardCharsets.UTF_8);
                                    return wikipedia.getDescription(wikiIdentifier);
                                });
                            }));

            Mono<List<Album>> albums = coverArt.getAlbums(releaseGroups);

            Mono<ArtistBand> artistBandMono = descriptionMono.zipWith(albums, (description, album) -> {
                return new ArtistBand(mbid, description, album);
            });

            LOGGER.debug("Returning artistBandMono");
            return artistBandMono.cache();

        }).switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid MBID: " + mbid)));

    }

    // // Returns the ID corresponding to the given tag from a List of Strings
    // // Either WikiData ID or Wikipedia ID. Null if not found
    private String findWikiTag(List<String> relations, String tag) {
        return relations.stream()
                .filter(str -> str.contains(tag))
                .map(str -> str.substring(str.lastIndexOf('/') + 1))
                .findAny()
                .orElse(null);
    }

}
