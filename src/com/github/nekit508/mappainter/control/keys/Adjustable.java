package com.github.nekit508.mappainter.control.keys;

import arc.Core;
import arc.input.KeyCode;
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

    default String settingsId() {
        return category().id() + ":" + id();
    }

    default boolean bindFilterValid(String filter) {
        return filter == null || id().contains(filter);
    }

    default void saveKeyCode(String field, KeyCode keyCode) {
        Core.settings.put(settingsId() + "." + field, keyCode == null ? -1 : keyCode.ordinal());
    }

    default KeyCode loadKeyCode(String field, KeyCode defaultKeyCode) {
        var ordinal = Core.settings.getInt(settingsId() + "." + field, -2);

        return switch (ordinal) {
            case -1 -> null;
            case -2 -> defaultKeyCode;
            default -> KeyCode.byOrdinal(ordinal);
        };
    }
}
