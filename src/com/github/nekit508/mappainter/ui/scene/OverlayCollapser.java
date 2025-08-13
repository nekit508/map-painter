package com.github.nekit508.mappainter.ui.scene;

import arc.Core;
import arc.func.Cons2;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.ui.layout.Table;

public class OverlayCollapser extends Element {
    public Table table;
    /** DO NOT modify without {@link OverlayCollapser#updateTable()} calling */
    protected boolean collapsed;

    public float yOffset;

    public OverlayCollapser(Cons2<Table, OverlayCollapser> cons, boolean collapsed) {
        this(cons, collapsed, Core.scene.root);
    }

    public OverlayCollapser(Cons2<Table, OverlayCollapser> cons, boolean collapsed, Group overlayParent) {
        table = new Table();
        cons.get(table, this);
        overlayParent.addChild(table);

        table.update(() -> {
            table.setWidth(table.getPrefWidth());
            table.setHeight(table.getPrefHeight());
        });

        setCollapsed(collapsed);
    }

    public void setCollapsed(boolean collapsed) {
        if (collapsed)
            table.toFront();

        this.collapsed = collapsed;
        updateTable();
    }

    public void toggle() {
        setCollapsed(!collapsed);
        updateTable();
    }

    protected Vec2 updateTable$temp$vec2$1 = new Vec2();
    public void updateTable() {
        table.visible = !collapsed;
        table.toFront();

        var tp = localToAscendantCoordinates(table.parent, updateTable$temp$vec2$1.set(x, y));
        table.x = tp.x;
        table.y = tp.y - height / 2 - table.getHeight();
    }

    @Override
    public void act(float delta) {
        super.act(delta);

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
