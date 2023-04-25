package com.example.mashup.services.clients;

import static org.junit.Assert.assertEquals;

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

public class WikipediaClientTest {

    private MockWebServer mockWebServer;
    private WikipediaClient wikipediaClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        wikipediaClient = new WikipediaClient(WebClient.create(mockWebServer.url("/").toString()));
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }


    @Test
    void testGetDescription() throws FileNotFoundException, IOException {
        String expectedDescription = "<p><b>Nirvana</b> was an American rock band formed in Aberdeen, Washington, in 1987. Founded by lead singer and guitarist Kurt Cobain and bassist Krist Novoselic, the band went through a succession of drummers, most notably Chad Channing, and then recruited Dave Grohl in 1990. Nirvana's success popularized alternative rock, and they were often referenced as the figurehead band of Generation X. Their music maintains a popular following and continues to influence modern rock culture.\n</p><p>In the late 1980s, Nirvana established itself as part of the Seattle grunge scene, releasing its first album, <i>Bleach</i>, for the independent record label Sub Pop in 1989. They developed a sound that relied on dynamic contrasts, often between quiet verses and loud, heavy choruses. After signing to major label DGC Records in 1991, Nirvana found unexpected mainstream success with \"Smells Like Teen Spirit\", the first single from their landmark second album <i>Nevermind</i> (1991). A cultural phenomenon of the 1990s, <i>Nevermind</i> was certified Diamond by the RIAA and is credited for ending the dominance of hair metal.</p><p>Characterized by their punk aesthetic, Nirvana's fusion of pop melodies with noise, combined with their themes of abjection and social alienation, brought them global popularity. Following extensive tours and the 1992 compilation album <i>Incesticide</i> and EP <i>Hormoaning</i>, the band released their highly anticipated third studio album, <i>In Utero</i> (1993). The album topped both the US and UK album charts, and was acclaimed by critics. Nirvana disbanded following Cobain's suicide in April 1994. Various posthumous releases have been overseen by Novoselic, Grohl, and Cobain's widow Courtney Love. The posthumous live album <i>MTV Unplugged in New York</i> (1994) won Best Alternative Music Performance at the 1996 Grammy Awards.\n</p><p>Nirvana is one of the best-selling bands of all time, having sold more than 75\u00a0million records worldwide. During their three years as a mainstream act, Nirvana received an American Music Award, Brit Award and Grammy Award, as well as seven MTV Video Music Awards and two NME Awards. They achieved five number-one hits on the <i>Billboard</i> Alternative Songs chart and four number-one albums on the <i>Billboard</i> 200. In 2004, <i>Rolling Stone</i> named Nirvana among the 100 greatest artists of all time. They were inducted into the Rock and Roll Hall of Fame in their first year of eligibility in 2014.\n</p>";

        ObjectMapper objectMapper = new ObjectMapper();
        File jsonFile = new File("src/test/resources/wikipedia.json");
        JsonNode jsonNode = objectMapper.readTree(new FileInputStream(jsonFile));

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonNode.toString())
                .addHeader("Content-Type", "application/json"));

                StepVerifier.create(wikipediaClient.getDescription("Nirvana_(band)"))
                .assertNext(response -> {
                    assertEquals(expectedDescription, response);
                }).verifyComplete();
    
    }

    @Test
    void testGetDescription_NotFound() throws FileNotFoundException, IOException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404));

                StepVerifier.create(wikipediaClient.getDescription("PageNotFound"))
                    .verifyComplete();
    
    }
}
