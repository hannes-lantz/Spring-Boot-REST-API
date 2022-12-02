package com.example.restservice.types;

import java.net.URL;

//Represents an Album
public class Album {
    private String id;
	private String title;
	private URL coverArt;

	public Album() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public URL getCoverArt() {
		return coverArt;
	}

	public void setCoverArt(URL coverArt) {
		this.coverArt = coverArt;
	}
}
