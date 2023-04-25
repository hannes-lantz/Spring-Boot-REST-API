package com.example.mashup.datamodels;

import java.util.ArrayList;
import java.util.List;

//Represents a Artist or a Band
public class ArtistBand {

	private String mbid, description;
	private List<Album> albums = new ArrayList<Album>();

	public ArtistBand() {
	}

	public ArtistBand(String mbid, String description, List<Album> albums) {
		this.mbid = mbid;
		this.description = description;
		this.albums = albums;
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

	public List<Album> getAlbums() {
		return albums;
	}

	public void setAlbums(List<Album> albums) {
		this.albums = albums;
	}

}
