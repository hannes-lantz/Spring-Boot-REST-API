package com.example.restful.services;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.restful.datamodels.Album;
import com.example.restful.datamodels.ArtistBand;

@Service
@ConfigurationProperties("musicbrainz")
public class MusicBrainzService {

    private static final String API_PATH = "/ws/2/artist/{mbid}?fmt=json&inc=url-rels+release-groups";
    private static Logger LOGGER = LoggerFactory.getLogger(MusicBrainzService.class);
    private String baseURI;

    public String getBaseURI() {
        return baseURI;
    }

    public void setBaseURI(String baseURI) {
        this.baseURI = baseURI;

    }

    @Autowired
    RestTemplate api;

    @Autowired
    private CoverArtService coverArtService;

    @Autowired
    private WikipediaService wikipediaService;

    @Autowired
    private WikiDataService wikiDataService;

    public ArtistBand getMusicBrainzData(String mbid) {

        LOGGER.debug("baseURI: {}", getBaseURI());
        Map response = api.getForObject(getBaseURI().concat(API_PATH), Map.class, mbid);
        List<Map> releaseGroupList = (List<Map>) response.get("release-groups");
        List<Map> relationList = (List<Map>) response.get("relations");

        List<String> relations = relationList.stream()
                .filter((url) -> url.get("url") != null)
                .map((res) -> res.get("url").toString())
                .collect(Collectors.toList());

        String wiki = findWikiTag(relations, "wikipedia");

        if (wiki == null) {
            String wikiData = findWikiTag(relations, "wikidata");

            if (wikiData != null) {
                wiki = wikiDataService.getWikiPage(wikiData);
            }
        }  
        
        Optional<String> description = Optional.empty();
        if (wiki != null){
            String wikiIdentifier = URLEncoder.encode(wiki.replaceAll("[()]", ""), StandardCharsets.UTF_8);
            description = wikipediaService.getDescription(wikiIdentifier);
        }

        //Gets the cover art for all albums
        //parallelStream() makes the process fast
        List<Album> albums = releaseGroupList.parallelStream()
                .filter((album) -> album.get("primary-type") != null)
                .map((album) -> new Album(
                        (String) album.get("id"),
                        (String) album.get("title"),
                        (String) coverArt((String) album.get("id"))))
                .collect(Collectors.toList());

        return new ArtistBand(
                (String) response.get("id"),
                (String) response.get("name"),
                description.orElse("<p>No description found</p>"),
                albums);
    }

    private String findWikiTag(List<String> relations, String tag) {
        String wikiId = findTag(relations, tag);

        if (wikiId == null) {
            return null;
        }

        wikiId = wikiId.substring(wikiId.lastIndexOf('/') + 1);
        wikiId = wikiId.substring(0, wikiId.length()-1);
        if(wikiId.contains(",")){
            return wikiId.substring(0, wikiId.lastIndexOf(','));
        }
        return wikiId;
    }

    //Returns a String that contains the given tag from a List of Strings
    private String findTag(List<String> relations, String tag) {
        return relations.stream()
                .filter(str -> str.contains(tag))
                .findAny()
                .orElse(null);
    }

    //Fetches Cover Art
    private String coverArt(String id) {
        return coverArtService.getCoverArt(id);
    }
}
