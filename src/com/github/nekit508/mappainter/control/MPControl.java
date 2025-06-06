package com.github.nekit508.mappainter.control;

import arc.Core;
import arc.Events;
import arc.input.KeyCode;
import arc.math.Interp;
import arc.scene.Action;
import arc.scene.Element;
import arc.scene.actions.Actions;
import arc.scene.event.Touchable;
import arc.scene.ui.ButtonGroup;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Table;
import arc.scene.ui.layout.WidgetGroup;
import arc.util.Time;
import arc.util.Tmp;
import com.github.nekit508.mappainter.core.MPCore;
import com.github.nekit508.mappainter.graphics.figure.FigureType;
import com.github.nekit508.mappainter.ui.scene.RadialMenu;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.input.Binding;
import mindustry.ui.Styles;

public class MPControl {
    public FigureType.Figure figure;
    public boolean locked = true;

    public Table mainTable, selectTable, creationTable;
    public Element mapOverlay;

    public WidgetGroup hudGroup;
    ButtonGroup<TextButton> buttons = new ButtonGroup<>();

    public RadialMenu menu;

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
        hudGroup.touchable = Touchable.enabled;
        hudGroup.visible(() -> !locked);
        Core.scene.add(hudGroup);

        hudGroup.fill(table -> {
            table.center().bottom();
            table.table(t -> {
                t.add(mapOverlay = new Element()).grow();
            }).grow();

            table.row();

            table.table(mt -> mainTable = mt).growX();
        });

        hudGroup.addChild(menu = new RadialMenu(menu -> {
            MPCore.figuresManager.figureTypes.each(type -> {
                menu.addButton(new RadialMenu.RadialMenuButton(){{
                    hideOnClick = true;
                    icon = type.icon;
                    checked = figure != null && type == figure.type;
                    listener = btn -> {
                        if (btn.checked)
                            unselectFigure();
                        else {
                            btn.parent.buttons.each(b -> b.checked = false);
                            figureSelected(type);
                        }
                        checked = !checked;
                        btn.parent.rebuild();
                    };
                }});
            });

            if (figure != null)
                menu.center(new RadialMenu.RadialMenuButton(){{
                    icon = Core.atlas.getDrawable("map-painter-add");
                    checked = false;
                    hideOnClick = false;
                    listener = btn -> {
                        figureCreated();
                        btn.parent.rebuild();
                    };
                }});
        }){{
            setFillParent(true);

            visible = false;
            setScale(0, 0);

            shown = menu -> {
                menu.ignoreInput = true;
                menu.rebuild();
                menu.actions(Actions.scaleTo(1, 1, 0.2f, Interp.pow3Out), new Action(){
                    @Override
                    public boolean act(float delta) {
                        ((RadialMenu) target).ignoreInput = false;
                        return true;
                    }
                });
            };

            hidden = menu -> {
                menu.visible = true;
                menu.actions(Actions.scaleTo(0, 0, 0.1f, Interp.pow3Out), Actions.hide());
            };
        }});
        hudGroup.clicked(KeyCode.mouseRight, () -> {
            if (menu.visible) {
                menu.hide();
            } else {
                menu.setPosition(Core.input.mouseX(), Core.input.mouseY());
                menu.show();
            }
        });

        build();
    }

    public void build() {
        mainTable.reset();

        mainTable.background(Styles.black8);

        mainTable.center().bottom();

        mainTable.image().color(Pal.accent).colspan(1).height(4).growX().padBottom(5).visible(() -> figure != null).colspan(2).row();

        mainTable.table(ct -> {
            creationTable = ct;
            ct.left();
        }).left().bottom().growX().padBottom(5).visible(() -> figure != null);

        mainTable.button(Icon.add, Styles.clearNonei, () -> {
            FigureType f = figure.type;
            figureCreated();
            if (Core.input.keyDown(KeyCode.shiftLeft) || Core.input.keyDown(KeyCode.shiftRight))
                figureSelected(f);
        }).padBottom(5).center().width(100).growY().visible(() -> figure != null);

        mainTable.row().image().color(Pal.accent).colspan(1).height(4).growX().padBottom(5).colspan(2).row();

        mainTable.table(st -> {
            selectTable = st;

            st.defaults().center().size(64);

            buttons.clear();
            buttons.setMaxCheckCount(1);
            buttons.setMinCheckCount(0);
            MPCore.figuresManager.figureTypes.each(type -> {
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
                type.constructSelectionButton(btn);

                st.add(btn);
            });
        }).center().bottom().growX().height(64).colspan(2);
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
            figure.updateCreation(creationTable);
        }
    }

    public void figureCreated() {
        if (figure == null)
            return;
        figure.created();
        MPCore.figuresManager.addFigure(figure);
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
        figure.constructCreationTable(creationTable);
        mapOverlay.addListener(figure);
    }
}
