package com.github.nekit508.mappainter.control;

import arc.Core;
import arc.input.KeyCode;
import arc.math.geom.Vec2;
import arc.scene.ui.Button;
import arc.scene.ui.ButtonGroup;
import arc.scene.ui.ImageButton;
import arc.scene.ui.layout.Table;
import com.github.nekit508.mappainter.content.MPFx;
import com.github.nekit508.mappainter.control.keys.keyboard.KeyBinding;
import com.github.nekit508.mappainter.map.generator.WFCBaseGenerator;
import com.github.nekit508.mappainter.ui.MPUI;
import com.github.nekit508.mappainter.ui.scene.OverlayCollapser;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.game.Team;
import mindustry.gen.Icon;
import mindustry.ui.Styles;

import java.lang.reflect.Field;

public class TesterControl extends ControlReceiver {
    public ButtonGroup<Button> effectSelectors;

    @Override
    public KeyBinding switchButton() {
        return MPKeyBindings.openTester;
    }

    @Override
    public void init() {
        super.init();

        effectSelectors = new ButtonGroup<>();
        effectSelectors.setMinCheckCount(0);

        group.fill(t -> {
            t.left().top();
            t.table(table -> {
                table.left().top();
                table.background(Styles.black3);

                OverlayCollapser c;
                table.stack(c = new OverlayCollapser(
                        (colTable, col) -> {
                            col.hideOnOutsideClick = false;

                            colTable.defaults().growX();
                            colTable.pane(pane -> {
                                pane.defaults().growX();

                                addEffectsFromClassFields(MPFx.class, pane);
                                addEffectsFromClassFields(Fx.class, pane);
                            });
                        }, true
                ), new ImageButton(Icon.book, Styles.emptyi){{
                    clicked(c::toggle);
                }}).left();

                table.button("sprite reloader", Styles.cleart, () -> {
                    MPUI.spriteReloaderDialog.toggle();
                }).fill().minWidth(300).minHeight(32);

                table.button("wfc generator", Styles.cleart, () -> {
                    new WFCBaseGenerator().generate(Team.crux);
                }).fill().minWidth(300).minHeight(32);
            }).fill();
        });

        group.clicked(KeyCode.mouseRight, () -> {
            if (effectSelectors.getChecked() != null)
                spawnEffectAt((Effect) effectSelectors.getChecked().userObject, Core.input.mouseWorld());
        });

        effectSelectors.uncheckAll();
    }

    public Table constructFxSpawnTable(Effect effect, String name) {
        var out = new Table();

        out.defaults().growX();

        out.label(() -> name);
        out.button(Icon.add, Styles.clearNoneTogglei, () -> {

        }).with(btn -> {
            effectSelectors.add(btn);
            btn.userObject = effect;
        }).size(Vars.iconMed).right();

        return out;
    }

    public void spawnEffectAt(Effect effect, Vec2 pos) {
        effect.at(pos.x, pos.y);
    }

    public void addEffectsFromClassFields(Class<?> clazz, Table effectsTable) {
        var fields = clazz.getFields();
        for (Field field : fields)
            if (Effect.class.isAssignableFrom(field.getType())) {
                try {
                    effectsTable.add(constructFxSpawnTable((Effect) field.get(null), field.getName()));
                    effectsTable.row();
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
    }
}
