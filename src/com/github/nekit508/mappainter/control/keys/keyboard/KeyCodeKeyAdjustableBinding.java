package com.github.nekit508.mappainter.control.keys.keyboard;

import arc.Core;
import arc.input.KeyCode;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.event.Touchable;
import arc.scene.ui.ButtonGroup;
import arc.scene.ui.ImageButton;
import arc.scene.ui.layout.Table;
import com.github.nekit508.mappainter.control.keys.Adjustable;
import com.github.nekit508.mappainter.ui.scene.OverlayCollapser;
import mindustry.Vars;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;

public interface KeyCodeKeyAdjustableBinding extends KeyCodeKeyBinding, Adjustable {
    @Override
    default void buildSettings(Table table) {
        table.label(() -> key() != null ? (key().toString() + ":" + key().ordinal()) : "nil").expand().center();

        table.addListener(new InputListener(){
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button) {
                key(button);
            }

            @Override
            public boolean keyUp(InputEvent event, KeyCode keycode) {
                key(keycode);
                return false;
            }
        });

        table.update(() -> {
            Core.scene.setKeyboardFocus(table);
            Core.scene.setScrollFocus(table);
        });
        table.touchablility = () -> Touchable.enabled;
    }

    @Override
    default void buildInfo(Table table) {
        table.label(() -> key() != null ? key().toString() : "nil").color(Pal.accent).right().minWidth(90).fillX().padRight(20);

        var collapser = new OverlayCollapser((t, c) -> {
            var typeSelectors = new ButtonGroup<>();
            typeSelectors.setMinCheckCount(1);
            typeSelectors.setMaxCheckCount(1);


            t.defaults().size(70, 30);
            t.button("down", Styles.clearTogglet, () -> {
                type(Type.down);
                save();
            }).with(b -> {
                typeSelectors.add(b);
                if (type() == Type.down)
                    b.toggle();
            });

            t.button("tap", Styles.clearTogglet, () -> {
                type(Type.tap);
                save();
            }).with(b -> {
                typeSelectors.add(b);
                if (type() == Type.tap)
                    b.toggle();
            });

            t.button("up", Styles.clearTogglet, () -> {
                type(Type.up);
                save();
            }).with(b -> {
                typeSelectors.add(b);
                if (type() == Type.up)
                    b.toggle();
            });

            t.defaults().reset();
            t.row();
            t.image().color(Pal.power).colspan(3).fillX();
        }, true);
        var typeButton = new ImageButton(Tex.buttonDown, Styles.emptyi);
        typeButton.resizeImage(Vars.iconMed);
        typeButton.clicked(collapser::toggle);

        table.stack(collapser, typeButton).size(Vars.iconMed).padRight(20);
    }

    @Override
    default boolean keyFilterValid(String filter) {
        return filter == null || (key() != null && key().toString().contains(filter));
    }

    @Override
    default void defaults() {
        key(defaultKey());
        type(defaultType());
    }

    @Override
    default void setNull() {
        key(null);
    }

    @Override
    default void save() {
        saveKeyCode("key", key());

        Core.settings.put(settingsId() + ".type", type().ordinal());
    }

    @Override
    default void load() {
        key(loadKeyCode("key", defaultKey()));

        var typeOrdinal = Core.settings.getInt(settingsId() + ".type", -1);
        if (typeOrdinal == -1)
            type(defaultType());
        else type(Type.values()[typeOrdinal]);
    }
}
