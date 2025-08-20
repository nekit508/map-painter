package com.github.nekit508.mappainter.control;

import arc.input.KeyCode;
import com.github.nekit508.mappainter.control.keys.Category;
import com.github.nekit508.mappainter.control.keys.keyboard.KeyCodeKeyAdjustableBinding;

public enum MPKeyBindings implements KeyCodeKeyAdjustableBinding {
    openPainter(KeyCode.l, Type.tap, "mp-open-painter", MPCategories.dialogs),
    openTester(KeyCode.f7, Type.tap, "mp-open-tester", MPCategories.dialogs),
    openObjectEditor(KeyCode.f12, Type.tap, "mp-open-object-editor", MPCategories.dialogs),

    moveBoost(KeyCode.shiftLeft, Type.tap, "mp-camera-move-boost", MPCategories.camera);

    private KeyCode bind;
    private Type type;
    private final KeyCode defaultBind;
    private final Type defaultType;
    private final String id;
    private final Category category;

    MPKeyBindings(KeyCode defaultBind, Type defaultType, String id, Category category) {
        this.defaultBind = defaultBind;
        this.defaultType = defaultType;
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
    public Type type() {
        return type;
    }

    @Override
    public void type(Type type) {
        this.type = type;
    }

    @Override
    public Type defaultType() {
        return defaultType;
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
