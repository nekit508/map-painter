package com.github.nekit508.mappainter.control.keys;

import arc.input.KeyCode;
import com.github.nekit508.mappainter.control.keys.keyboard.KeyCodeKeyboardAdjustableBinding;

public enum MPKeyboardBindings implements KeyCodeKeyboardAdjustableBinding {
    openPainter(KeyCode.l, "open-painter", MPCategories.baseBindings),
    openTester(KeyCode.f7, "open-tester", MPCategories.baseBindings),
    moveBoost(KeyCode.shiftLeft, "camera-move-boost", MPCategories.camera);

    private KeyCode bind;
    private final KeyCode defaultBind;
    private final String id;
    private final Category category;

    MPKeyboardBindings(KeyCode defaultBind, String id, Category category) {
        this.defaultBind = defaultBind;
        this.id = id;
        this.category = category;
        created();
    }

    @Override
    public KeyCode key() {
        return bind;
    }

    @Override
    public void key(KeyCode key) {
        bind = key;
    }

    @Override
    public KeyCode defaultKey() {
        return defaultBind;
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
