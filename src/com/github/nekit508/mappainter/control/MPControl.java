package com.github.nekit508.mappainter.control;

import arc.Core;
import arc.Events;
import arc.input.KeyCode;
import arc.scene.Element;
import arc.scene.event.Touchable;
import arc.scene.ui.ButtonGroup;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Table;
import arc.scene.ui.layout.WidgetGroup;
import arc.util.Time;
import arc.util.Tmp;
import com.github.nekit508.mappainter.core.MPCore;
import com.github.nekit508.mappainter.graphics.figure.FigureType;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.input.Binding;
import mindustry.ui.Styles;

public class MPControl {
    public FigureType.Figure figure;
    public boolean locked = true;

    public Table mainTable, selectTable, creationTable, mapOverlay;

    public WidgetGroup hudGroup;
    ButtonGroup<TextButton> buttons = new ButtonGroup<>();

    public MPControl() {
        Events.run(EventType.Trigger.update, () -> {
            if (Vars.state.isGame())
                update();
            else // for fix main menu
                updateState(true);
        });

        Vars.control.input.inputLocks.add(() -> !locked);
    }

    public void init() {
        hudGroup = new WidgetGroup();
        hudGroup.setFillParent(true);
        hudGroup.touchable = Touchable.childrenOnly;
        hudGroup.visible(() -> !locked);
        Core.scene.add(hudGroup);

        hudGroup.fill(table -> {
            table.center().bottom();
            table.table(t -> {
                mapOverlay = t;
                t.add(new Element()).grow();
            }).grow();

            table.row();

            table.table(mt -> {
                mainTable = mt;
                mt.background(Styles.black8);

                mt.center().bottom();

                mt.image().color(Pal.accent).colspan(1).height(4).growX().padBottom(5).visible(() -> figure != null).colspan(2).row();

                mt.table(ct -> {
                    creationTable = ct;
                    ct.left();
                }).left().bottom().growX().padBottom(5).visible(() -> figure != null);

                mt.button(Icon.add, Styles.clearNonei, this::figureCreated).padBottom(5).center().width(100).growY().visible(() -> figure != null);

                mt.row().image().color(Pal.accent).colspan(1).height(4).growX().padBottom(5).colspan(2).row();

                mt.table(st -> {
                    selectTable = st;

                    st.defaults().center().size(64);

                    buttons.clear();
                    buttons.setMaxCheckCount(1);
                    buttons.setMinCheckCount(0);
                    FigureType.figureTypes.each(type -> {
                        TextButton btn = new TextButton("", Styles.flatToggleMenut);
                        btn.clearChildren();
                        buttons.add(btn);

                        btn.clicked(() -> {
                            if (!btn.isChecked())
                                unselectFigure();
                            else
                                figureSelected(type);
                        });

                        btn.center();
                        btn.defaults().center().grow();
                        type.ConstructSelectionButton(btn);

                        st.add(btn);
                    });
                }).center().bottom().growX().height(64).colspan(2);
            }).growX();
        });

    }

    /** Called on locked is changed. */
    float defaultMinZoom = Vars.renderer.minZoom;
    public void updateState(boolean newState) {
        if (locked != newState)
            locked = newState;

        Vars.ui.hudfrag.shown = locked;
        if (locked) {
            Vars.renderer.minZoom = defaultMinZoom;
        } else {
            defaultMinZoom = Vars.renderer.minZoom;
            Vars.renderer.minZoom = 0.5f;
        }
    }

    public void update() {
        if (Vars.state.isGame() && Core.input.keyTap(KeyCode.l))
            updateState(!locked);
        if (locked) return;

        Core.camera.position.add(Tmp.v1.setZero().add(Core.input.axis(Binding.move_x), Core.input.axis(Binding.move_y)).nor().scl((!Core.input.keyDown(Binding.boost) ? 15f : 45f) * Time.delta));

        if (figure != null) {
            if (figure.created())
                figureCreated();
            else
                figure.updateCreation(creationTable);
        }
    }

    public void figureCreated() {
        MPCore.renderer.add(figure);
        unselectFigure();
        buttons.uncheckAll();
    }

    public void unselectFigure() {
        if (figure == null) return;

        creationTable.reset();
        mapOverlay.removeListener(figure);
        figure = null;
    }

    public void figureSelected(FigureType newFigureType) {
        unselectFigure();

        figure = newFigureType.create();
        figure.onCreate(creationTable);
        mapOverlay.addListener(figure);
    }
}
