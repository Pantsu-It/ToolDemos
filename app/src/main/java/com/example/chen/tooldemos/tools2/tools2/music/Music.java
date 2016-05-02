package com.example.chen.tooldemos.tools2.tools2.music;

/**
 * Created by chen on 16/5/1.
 */

/**
 * Created by chen on 16/4/30.
 */
public class Music {
    private int id;
    private String title;
    private String album;
    private int albumId;
    private String artist;
    private String path;
    private String displayName;
    private String mimeType;
    private long duaration;
    private long size;

    public Music(int id, String title, String album, int albumId, String artist, String path, String displayName, String mimeType, long duaration, long size) {
        this.id = id;
        this.title = title;
        this.album = album;
        this.albumId = albumId;
        this.artist = artist;
        this.path = path;
        this.displayName = displayName;
        this.mimeType = mimeType;
        this.duaration = duaration;
        this.size = size;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public int getAlbumId() {
        return albumId;
    }

    public void setAlbumId(int albumId) {
        this.albumId = albumId;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public long getDuaration() {
        return duaration;
    }

    public void setDuaration(long duaration) {
        this.duaration = duaration;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
