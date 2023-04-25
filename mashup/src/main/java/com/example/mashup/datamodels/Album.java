package com.example.mashup.datamodels;

//Represents an Album
public class Album {
    private String id, title, coverArt;

	public Album() {
	}

	public Album(String id, String title, String coverArt) {
		this.id = id;
        this.title = title;
        this.coverArt = coverArt;
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

	public String getCoverArt() {
		return coverArt;
	}

	public void setCoverArt(String coverArt) {
		this.coverArt = coverArt;
	}
}
