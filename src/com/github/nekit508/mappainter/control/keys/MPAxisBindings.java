package com.github.nekit508.mappainter.control.keys;

import arc.input.KeyCode;
import com.github.nekit508.mappainter.control.keys.axis.KeyCodeAxisAdjustableBinding;

public enum MPAxisBindings implements KeyCodeAxisAdjustableBinding {
    moveX(MPCategories.camera, "camera-move-x", KeyCode.d, KeyCode.a),
    moveY(MPCategories.camera, "camera-move-y", KeyCode.w, KeyCode.s);

    public final Category category;
    public final String id;

    public final KeyCode defaultUp, defaultDown;
    public KeyCode up, down;

    MPAxisBindings(Category category, String id, KeyCode defaultUp, KeyCode defaultDown) {
        this.category = category;
        this.id = id;
        this.defaultUp = defaultUp;
        this.defaultDown = defaultDown;
        created();
    }

    @Override
    public KeyCode up() {
        return up;
    }

    @Override
    public KeyCode down() {
        return down;
    }

    @Override
    public void up(KeyCode key) {
        up = key;
    }

    @Override
    public void down(KeyCode key) {
        down = key;
    }

    @Override
    public KeyCode defaultUp() {
        return defaultUp;
    }

    @Override
    public KeyCode defaultDown() {
        return defaultDown;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public Category category() {
        return category;
    }
}
