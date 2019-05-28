package com.apps.kunalfarmah.myapplication;

public class file_struct {
    String name;
    String size;

    public file_struct(String name, String size, String path) {
        this.name = name;
        this.size = size;
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    String path;

    public file_struct(String name, String size) {
        this.name = name;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
