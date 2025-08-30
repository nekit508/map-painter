package com.github.nekit508.mappainter.control;

import com.github.nekit508.emkb.control.keys.Category;

public enum MPCategories implements Category {
    dialogs("mp-dialogs"),
    camera("mp-camera");

    private final String id;

    MPCategories(String id) {
        this.id = id;
    }

    @Override
    public String id() {
        return id;
    }
}
