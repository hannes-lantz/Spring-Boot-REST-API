package com.example.restful.controller;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


import com.example.restful.services.MusicBrainzService;
import com.example.restful.datamodels.ArtistBand;

@Controller
@RequestMapping("/api/{mbid}")
public class ArtistBandController {

	@Autowired
    private MusicBrainzService musicBrainzService;

	@RequestMapping(method = RequestMethod.GET)
	public @ResponseBody ArtistBand getData(@PathVariable("mbid") String mbid) {
		// Get artist from MusicBrainz
		return musicBrainzService.getMusicBrainzData(mbid);
	}
}
