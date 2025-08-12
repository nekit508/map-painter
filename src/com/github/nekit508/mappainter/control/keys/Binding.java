package com.github.nekit508.mappainter.control.keys;

import arc.Core;
import arc.input.KeyCode;

/** {@link Binding#created()} must be executed on binding creating. */
public interface Binding {
    String id();
    Category category();

    default String settingsId() {
        return category().id() + ":" + id();
    }

    default void created() {
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

    default boolean keyTapped(KeyCode key) {
        return key != null && Core.input.keyTap(key);
    }

    default boolean keyDown(KeyCode key) {
        return key != null && Core.input.keyDown(key);
    }

    default boolean keyUp(KeyCode key) {
        return key != null && Core.input.keyRelease(key);
    }
}
