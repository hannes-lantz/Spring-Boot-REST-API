package com.example.restservice.types;

import java.util.List;

//Represents a Artist or a Band
public class ArtistBand {
    
    private String mbid;
    private String description;
    //private List<Album> albums;

    public ArtistBand() {
	}

	public String getMbid() {
		return mbid;
	}

	public void setMbid(String mbid) {
		this.mbid = mbid;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	// public List<Album> getAlbums() {
	// 	return albums;
	// }

	// public void setAlbums() {
		
	// }



}
