package com.example.mashup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import com.example.mashup.datamodels.ArtistBand;
import com.example.mashup.services.MashupService;

import reactor.core.publisher.Mono;



@Controller
@RequestMapping("/api")
public class MashupController {

    @Autowired
    private MashupService musicBrainzService;
    
    @CrossOrigin()
    @GetMapping("/{mbid}")
    public @ResponseBody Mono<ArtistBand> getMashup(@PathVariable String mbid) {
        return musicBrainzService.getArtistBand(mbid);
    }
}
