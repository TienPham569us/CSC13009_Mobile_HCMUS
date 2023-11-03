package com.example.imagesgallery.Model;

import java.io.Serializable;

// class tạo tạm thời do chưa load được hình ảnh từ csdl
public class Image implements Serializable {
    private String path;
    private String description;
    private String id_album;
    private int isFavored;

    public Image(String path, String description, String id_album, int isFavored) {
        this.path = path;
        this.description = description;
        this.id_album = id_album;
        this.isFavored = isFavored;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId_album() {
        return id_album;
    }

    public void setId_album(String id_album) {
        this.id_album = id_album;
    }

    public int getIsFavored() {
        return isFavored;
    }

    public void setIsFavored(int isFavored) {
        this.isFavored = isFavored;
    }
}
