package com.github.nekit508.mappainter.control.keys.keyboard;

import arc.input.KeyCode;

public interface KeyCodeKeyBinding extends KeyBinding {
    KeyCode key();
    void key(KeyCode key);
    KeyCode defaultKey();

    Type type();
    void type(Type type);
    Type defaultType();

    @Override
    default boolean active() {
        return switch (type()) {
            case down -> keyDown(key());
            case tap -> keyTapped(key());
            case up -> keyUp(key());
        };
    }

    enum Type {
        down, tap, up
    }
}
