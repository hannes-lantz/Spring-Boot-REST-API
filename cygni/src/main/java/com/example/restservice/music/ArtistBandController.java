package com.example.restservice.music;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.restservice.services.MusicBrainz;
import com.example.restservice.types.ArtistBand;

@RestController
@RequestMapping("/api/{mbid}")
public class ArtistBandController {

	@Autowired
	private MusicBrainz mbService;

	
	@RequestMapping(method = RequestMethod.GET)
	public @ResponseBody ArtistBand getData(@PathVariable("mbid") String mbid) {
		//Get artist from MusicBrainz
		MusicBrainz.ArtistBand mbArtist = mbService.findArtistBand(mbid);
		if (mbArtist == null) {
			return null;
		}

		//Create new artist
		ArtistBand artist = new ArtistBand();
		artist.setMbid(mbid);

		return artist;
	}
}