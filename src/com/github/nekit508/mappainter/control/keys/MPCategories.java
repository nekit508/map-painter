package com.github.nekit508.mappainter.control.keys;

public enum MPCategories implements Category {
    baseBindings("mp-base-bindings"),
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
