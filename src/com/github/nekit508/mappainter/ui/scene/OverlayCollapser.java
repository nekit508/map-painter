package com.github.nekit508.mappainter.ui.scene;

import arc.Core;
import arc.func.Boolp;
import arc.func.Cons2;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.ui.layout.Table;
import arc.util.Nullable;

public class OverlayCollapser extends Element {
    public Table table;
    /** DO NOT modify without {@link OverlayCollapser#updateTable()} calling */
    protected boolean collapsed;
    protected @Nullable Boolp isCollapsed = null;
    public boolean hideOnOutsideClick = true;

    public float yOffset;

    public OverlayCollapser(Cons2<Table, OverlayCollapser> cons, boolean collapsed) {
        this(cons, collapsed, Core.scene.root);
    }

    public OverlayCollapser(Cons2<Table, OverlayCollapser> cons, boolean collapsed, Group overlayParent) {
        table = new Table() {
            @Override
            public void act(float delta) {
                super.act(delta);

                if (!OverlayCollapser.this.isOnScene()) {
                    clear();
                    remove();
                }
            }
        };

        cons.get(table, this);
        overlayParent.addChild(table);
        table.visible(() -> isVisible() && !OverlayCollapser.this.collapsed);

        setCollapsed(collapsed);
    }

    protected boolean isOnScene() {
        for (Element cur = this; cur != null; cur = cur.parent)
            if (cur == Core.scene.root)
                return true;
        return false;
    }

    protected boolean isVisible() {
        for (Element cur = this; cur != null; cur = cur.parent) {
            cur.updateVisibility();
            if (!cur.visible)
                return false;
        }
        return true;
    }

    public void collapsed(Boolp isCollapsed) {
        this.isCollapsed = isCollapsed;
    }

    public void setCollapsed(boolean collapsed) {
        if (this.collapsed == collapsed) return;

        this.collapsed = collapsed;
        updateTable();
    }

    public void toggle() {
        setCollapsed(!collapsed);
        updateTable();
    }

    protected Vec2 updateTable$temp$vec2$1 = new Vec2();
    public void updateTable() {
        table.setWidth(table.getPrefWidth());
        table.setHeight(table.getPrefHeight());

        var tp = localToAscendantCoordinates(table.parent, updateTable$temp$vec2$1.set(x, y));
        table.x = tp.x;
        table.y = tp.y - height / 2 - table.getHeight();

        table.toFront();
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        // TODO fix bug (yep i should remember what bug it is)
        if (hideOnOutsideClick && Core.input.justTouched() && !collapsed)
            if (!table.hasMouse())
                setCollapsed(true);

        if (isCollapsed != null)
            setCollapsed(isCollapsed.get());

        updateTable();
    }

    public void destroyOverlayTable() {
        table.clear();
        table.remove();
    }

    @Override
    public boolean remove() {
        clear();
        return super.remove();
    }

    @Override
    public void clear() {
        destroyOverlayTable();
        super.clear();
    }
}
