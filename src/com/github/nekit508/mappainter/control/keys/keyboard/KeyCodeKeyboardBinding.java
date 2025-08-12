package com.github.nekit508.mappainter.control.keys.keyboard;

import arc.input.KeyCode;

public interface KeyCodeKeyboardBinding extends KeyboardBinding {
    KeyCode key();
    void key(KeyCode key);
    KeyCode defaultKey();

    @Override
    default boolean active() {
        return keyTapped(key());
    }
}
