package com.example.imagesgallery.Model;

import java.io.Serializable;

public class Album implements Serializable {
    private Image cover;
    private String name;
    private String description;

    public Album(Image cover, String name, String description) {
        this.cover = cover;
        this.name = name;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Image getCover() {
        return cover;
    }

    public void setCover(Image cover) {
        this.cover = cover;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
