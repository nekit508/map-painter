package com.github.nekit508.mappainter.control.keys.axis;

import arc.input.KeyCode;

public interface KeyCodeAxisBinding extends AxisBinding {
    KeyCode up();
    KeyCode down();

    void up(KeyCode key);
    void down(KeyCode key);

    KeyCode defaultUp();
    KeyCode defaultDown();

    @Override
    default float axis() {
        var out = 0f;

        if (keyDown(up())) out += 1f;
        if (keyDown(down())) out -= 1f;

        return out;
    }
}
