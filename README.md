## Mashup REST API using Spring Boot by Hannes Lantz.

Combines information from the following four sources into one REST API:

* MusicBrainz (http://musicbrainz.org/ws/2)
* WikiData (https://www.wikidata.org/w/api.php)
* Wikipedia (https://en.wikipedia.org/w/api.php)
* Cover Art Archive (http://coverartarchive.org)

## Build and Run
It is using a maven-wrapper so it should not matter which version of maven you are running on your machine. However you need to use Java 17. Navigate to the /mashup folder. In order to check if the dependencies are correct.

Run:
```bash
./mvnw -v
```

Output:
```java
Apache Maven 3.8.6 (84538c9988a25aec085021c365c560670ad80f63)
Maven home: /home/username/.m2/wrapper/dists/apache-maven-3.8.6-bin/1ks0nkde5v1pk9vtc31i9d0lcd/apache-maven-3.8.6
Java version: 17.0.5, vendor: Private Build, runtime: /usr/lib/jvm/java-17-openjdk-amd64
Default locale: en, platform encoding: UTF-8
OS name: "linux", version: "4.4.0-19041-microsoft", arch: "amd64", family: "unix"
```
If you get a different response than shown above, the following command could cause an error (mainly if you are not using Java 17).

```bash
./mvnw clean install
```

If this command went well, you can start the program with:

```bash
./mvnw spring-boot:run
```

## Usage

Open a seperate terminal window or try without "curl" inside your web browser.
```java
# returns 'information about Nirvana'
curl http://localhost:8080/api/5b11f4ce-a62d-471e-81fc-a69a8278c7da

# returns 'information about Daft Punk'
curl http://localhost:8080/api/056e4f3e-d505-4dad-8ec1-d04f521cbb56
``` 

Response JSON when using "curl http://localhost:8080/api/5b11f4ce-a62d-471e-81fc-a69a8278c7da":
```json
{
  "mbid": "5b11f4ce-a62d-471e-81fc-a69a8278c7da",
  "description": "<p><b>Nirvana</b> was an American rock band formed in Aberdeen, Washington, in 1987. Founded by lead singer and guitarist Kurt Cobain and bassist Krist Novoselic, the band went through a succession of drummers, most notably Chad Channing, and then recruited Dave Grohl in 1990. Nirvana's success popularized alternative rock, and they were often referenced as the figurehead band of Generation X. Their music maintains a popular following and continues to influence modern rock culture.\n</p><p>In the late 1980s, Nirvana established itself as part of the Seattle grunge scene, releasing its first album, <i>Bleach</i>, for the independent record label Sub Pop in 1989. They developed a sound that relied on dynamic contrasts, often between quiet verses and loud, heavy choruses. After signing to major label DGC Records in 1991, Nirvana found unexpected mainstream success with \"Smells Like Teen Spirit\", the first single from their landmark second album <i>Nevermind</i> (1991). A cultural phenomenon of the 1990s, <i>Nevermind</i> was certified Diamond by the RIAA and is credited for ending the dominance of hair metal.</p><p>Characterized by their punk aesthetic, Nirvana's fusion of pop melodies with noise, combined with their themes of abjection and social alienation, brought them global popularity. Following extensive tours and the 1992 compilation album <i>Incesticide</i> and EP <i>Hormoaning</i>, the band released their highly anticipated third studio album, <i>In Utero</i> (1993). The album topped both the US and UK album charts, and was acclaimed by critics. Nirvana disbanded following Cobain's suicide in April 1994. Various posthumous releases have been overseen by Novoselic, Grohl, and Cobain's widow Courtney Love. The posthumous live album <i>MTV Unplugged in New York</i> (1994) won Best Alternative Music Performance at the 1996 Grammy Awards.\n</p><p>Nirvana is one of the best-selling bands of all time, having sold more than 75 million records worldwide. During their three years as a mainstream act, Nirvana received an American Music Award, Brit Award and Grammy Award, as well as seven MTV Video Music Awards and two NME Awards. They achieved five number-one hits on the <i>Billboard</i> Alternative Songs chart and four number-one albums on the <i>Billboard</i> 200. In 2004, <i>Rolling Stone</i> named Nirvana among the 100 greatest artists of all time. They were inducted into the Rock and Roll Hall of Fame in their first year of eligibility in 2014.\n</p>",
  "albums": [
    {
      "id": "d1e29cfc-b6d5-4fe4-b0be-d7dae9f20ec3",
      "title": "Jam On Sunset",
      "coverArt": "No cover art found"
    },
    {
      "id": "fb3770f6-83fb-32b7-85c4-1f522a92287e",
      "title": "MTV Unplugged in New York",
      "coverArt": "http://coverartarchive.org/release/e5edb300-0df6-4089-87ae-ab4bfd20b66d/5930727027.jpg"
    },
    {
      "id": "f1afec0b-26dd-3db5-9aa1-c91229a74a24",
      "title": "Bleach",
      "coverArt": "http://coverartarchive.org/release/7d166a44-cfb5-4b08-aacb-6863bbe677d6/1247101964.jpg"
    }

    //more albums
  ]
}
```

## Test

Use the following command to run all tests:

```bash
./mvnw test
```

## Design

This is my first time creating a Spring Boot application. It has been very fun and challenging to solve this task. I have learned a lot about Spring Boot.

#### Frameworks and Tools
 * Spring Boot 3.0.1
 * Spring Framework
 * Spring WebFlux 
 * Reactor
 * JUnit 5
 * Mockito
 * MockwebServer
 * Maven


#### Design choices  

The program uses Mono and Flux in order to be asynchronous so that it can multiple requests to be handled simultaneously, improving scalability and performance. It can also handle long-running requests without blocking the server thread, improving resource utilization and user experience. One more technique used in order to improve performance and user experience is caching, which allows the system to make fewer calls to external APIs.

As long as the program can get a valid response from the MusicBrainz API will it return a JSON result even if wikidata, Wikipedia, or coverartarchive fail to get a valid response. This is because I do not think the whole program should fail to produce a response if only one of the external services causes an error. I think if I were a user of this service I would be happier if I would receive a partially completed response rather than no response.  

If wikidata or wikipedia fails


#### Test implementation

Some of the tests use Mockito to mock responses from other parts of the program. MockWebServer is used in order to mock responses from external APIs. Some of those mocked responses are copies of correct responses from those external API services. 

#### Improvements

The area that I would like to improve is error handling. Right now most of my services just return an Empty Mono when an error occurs. The getArtistBand() method in the MashupService class will return a Mono.error(ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid MBID: " + mbid)) if it fails to retrieve a Mono<Artistband>. That error will be displayed for the user. I would like to extend it so that it could handle more error codes etc. But I am not 100% sure when to handle or throw errors and which error. So this is something that I would like to learn more about and improve.  
