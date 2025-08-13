package com.github.nekit508.mappainter.control.keys;

import arc.Core;
import arc.input.KeyCode;

public interface Binding {
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
