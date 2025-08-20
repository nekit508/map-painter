package com.github.nekit508.mappainter.control.keys.axis;

import arc.Core;
import arc.input.KeyCode;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.ui.layout.Table;
import arc.util.Align;
import com.github.nekit508.mappainter.control.keys.Adjustable;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;

public interface KeyCodeAxisAdjustableBinding extends KeyCodeAxisBinding, Adjustable {
    @Override
    default void buildSettings(Table table) {
        table.table(t -> {
            t.label(() -> up() != null ? (up().toString() + ":" + up().ordinal()) : "nil").grow().center().with(l -> l.setAlignment(Align.center));
            t.addListener(new InputListener(){
                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button) {
                    up(button);
                }

                @Override
                public boolean keyUp(InputEvent event, KeyCode keycode) {
                    up(keycode);
                    return false;
                }
            });
            t.update(() -> {
                if (t.hasMouse()) {
                    Core.scene.setKeyboardFocus(t);
                    Core.scene.setScrollFocus(t);
                }
            });
        }).grow().row();
        table.image().color(Pal.power).growX().row();;
        table.table(t -> {
            t.label(() -> down() != null ? (down().toString() + ":" + down().ordinal()) : "nil").grow().center().with(l -> l.setAlignment(Align.center));
            t.addListener(new InputListener(){
                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button) {
                    down(button);
                }

                @Override
                public boolean keyUp(InputEvent event, KeyCode keycode) {
                    down(keycode);
                    return false;
                }
            });
            t.update(() -> {
                if (t.hasMouse()) {
                    Core.scene.setKeyboardFocus(t);
                    Core.scene.setScrollFocus(t);
                }
            });
        }).grow();
    }

    @Override
    default void created() {
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
