package com.github.nekit508.mappainter.ui.scene;

import arc.func.Cons2;
import arc.scene.ui.layout.Collapser;
import arc.scene.ui.layout.Table;

public class CollapserWithHeader extends Table {
    public Collapser collapser;
    public boolean animateCollapser;

    public CollapserWithHeader(
            Cons2<Table, CollapserWithHeader> collapserBuilder,
            Cons2<Table, CollapserWithHeader> headerBuilder,
            Cons2<Boolean, CollapserWithHeader> onCollapserToggle,
            boolean defaultCollapsed, boolean animate
    ) {
        collapser = new Collapser(t -> collapserBuilder.get(t, this), defaultCollapsed);
        animateCollapser = animate;

        defaults().growX();

        table(header -> {
            headerBuilder.get(header, this);

            /*header.button(Icon.downOpen, Styles.emptyi, () -> {
                        collapser.toggle(animateCollapser);
                        onCollapserToggle.get(collapser.isCollapsed(), this);
                    })
                    .update(i -> i.getStyle().imageUp = (!collapser.isCollapsed() ? Icon.upOpen : Icon.downOpen)).size(40f).right();*/
        });
        row();

        add(collapser);
    }
}
