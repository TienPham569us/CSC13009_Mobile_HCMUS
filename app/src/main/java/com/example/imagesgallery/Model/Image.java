package com.example.imagesgallery.Model;

import java.io.Serializable;

// class tạo tạm thời do chưa load được hình ảnh từ csdl
public class Image implements Serializable {
    private String path;
    private String description;
    private int isFavored;

    public Image(String path, String description, int isFavored) {
        this.path = path;
        this.description = description;
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

    public int getIsFavored() {
        return isFavored;
    }

    public void setIsFavored(int isFavored) {
        this.isFavored = isFavored;
    }
}
