package com.example.imagesgallery.Model;

// class tạo tạm thời do chưa load được hình ảnh từ csdl
public class Image {
    int source;

    public Image(int source) {
        this.source = source;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }
}
