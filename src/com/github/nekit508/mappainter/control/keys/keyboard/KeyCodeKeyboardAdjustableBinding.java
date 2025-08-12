package com.github.nekit508.mappainter.control.keys.keyboard;

import arc.Core;
import arc.scene.event.InputEvent;
import arc.scene.event.Touchable;
import arc.scene.ui.layout.Table;
import com.github.nekit508.mappainter.control.keys.Adjustable;
import mindustry.graphics.Pal;

public interface KeyCodeKeyboardAdjustableBinding extends KeyCodeKeyboardBinding, Adjustable {
    @Override
    default void buildSettings(Table table) {
        table.touchablility = () -> Touchable.enabled;
        table.addListener(e -> {
            if (e instanceof InputEvent event) {
                if (event.type == InputEvent.InputEventType.keyTyped) {
                    key(event.keyCode);
                }
            }

            return false;
        });
        table.label(() -> key() != null ? (key().toString() + ":" + key().ordinal()) : "nil").expand().center();
        table.update(() -> Core.scene.setKeyboardFocus(table));
    }

    @Override
    default void created() {
        KeyCodeKeyboardBinding.super.created();
        Adjustable.super.created();
    }

    @Override
    default void buildInfo(Table table) {
        table.label(() -> key() != null ? key().toString() : "nil").color(Pal.accent).right().minWidth(90).fillX().padRight(20);
    }

    @Override
    default boolean keyFilterValid(String filter) {
        return filter == null || (key() != null && key().toString().contains(filter));
    }

    @Override
    default void defaults() {
        key(defaultKey());
    }

    @Override
    default void setNull() {
        key(null);
    }

    @Override
    default void save() {
        saveKeyCode("key", key());
    }

    @Override
    default void load() {
        key(loadKeyCode("key", defaultKey()));
    }
}
