package com.example.imagesgallery.Model;

public class Album {
    private Image image;
    private String name;

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Album(Image image, String name) {
        this.image = image;
        this.name = name;
    }
}
