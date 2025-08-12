package com.github.nekit508.mappainter.control.keys;

import arc.scene.ui.layout.Table;

public interface Adjustable {
    String id();
    Category category();

    void buildSettings(Table table);
    void buildInfo(Table table);

    void save();
    void load();
    void defaults();
    void setNull();

    default void created() {
        load();
    }

    boolean keyFilterValid(String filter);

    default boolean bindFilterValid(String filter) {
        return filter == null || id().contains(filter);
    }
}
