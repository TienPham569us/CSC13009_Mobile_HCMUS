package com.example.imagesgallery.Model;

import java.io.Serializable;
import java.util.Date;

public class Image implements Serializable {
    private String path;
    private String description;
    private int isFavored;
    private boolean canAddToCurrentAlbum;

    private Date date;
    private long size;
    private String type;



    public Date getDate() {
        return date;
    }

    public long getSize() {
        return size;
    }

    public String getType() {
        return type;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Image(String path, String description, int isFavored) {
        this.path = path;
        this.description = description;
        this.isFavored = isFavored;
        canAddToCurrentAlbum = true;
    }
    public Image(String path, String description, int isFavored, Date date, long size, String type) {
        this.path = path;
        this.description = description;
        this.isFavored = isFavored;
        this.date = date;
        this.size = size;
        this.type = type;
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

    public int getIsFavored() {
        return isFavored;
    }

    public void setIsFavored(int isFavored) {
        this.isFavored = isFavored;
    }

    public boolean isCanAddToCurrentAlbum() {
        return canAddToCurrentAlbum;
    }

    public void setCanAddToCurrentAlbum(boolean canAddToCurrentAlbum) {
        this.canAddToCurrentAlbum = canAddToCurrentAlbum;
    }
}
