package com.example.imagesgallery.Model;

import java.io.Serializable;

// class tạo tạm thời do chưa load được hình ảnh từ csdl
public class Image implements Serializable {
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
