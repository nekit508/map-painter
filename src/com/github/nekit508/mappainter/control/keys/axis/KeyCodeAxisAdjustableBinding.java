package com.github.nekit508.mappainter.control.keys.axis;

import arc.scene.ui.layout.Table;
import com.github.nekit508.mappainter.control.keys.Adjustable;
import mindustry.graphics.Pal;

public interface KeyCodeAxisAdjustableBinding extends KeyCodeAxisBinding, Adjustable {
    @Override
    default void buildSettings(Table table) {

    }

    @Override
    default void created() {
        KeyCodeAxisBinding.super.created();
        Adjustable.super.created();
    }

    @Override
    default void buildInfo(Table table) {
        table.labelWrap(() -> up() != null ? up().toString() : "nil").color(Pal.accent).right().minWidth(90).fillX().padRight(20);
        table.labelWrap(() -> down() != null ? down().toString() : "nil").color(Pal.accent).right().minWidth(90).fillX().padRight(20);
    }

    @Override
    default boolean keyFilterValid(String filter) {
        return filter == null || ((up() != null && up().toString().contains(filter)) || (down() != null && down().toString().contains(filter)));
    }

    @Override
    default void setNull() {
        up(null);
        down(null);
    }

    @Override
    default void defaults() {
        up(defaultUp());
        down(defaultDown());
    }

    @Override
    default void save() {
        saveKeyCode("up", up());
        saveKeyCode("down", down());
    }

    @Override
    default void load() {
        up(loadKeyCode("up", defaultUp()));
        down(loadKeyCode("down", defaultDown()));
    }
}
