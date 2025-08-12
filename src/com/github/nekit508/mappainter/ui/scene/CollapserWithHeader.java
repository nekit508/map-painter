package com.github.nekit508.mappainter.ui.scene;

import arc.func.Boolc;
import arc.func.Cons;
import arc.scene.ui.layout.Collapser;
import arc.scene.ui.layout.Table;
import mindustry.gen.Icon;
import mindustry.ui.Styles;

public class CollapserWithHeader extends Table {
    public Collapser collapser;
    public boolean animateCollapser;

    public CollapserWithHeader(Cons<Table> collapserBuilder, Cons<Table> headerBuilder, Boolc onCollapserToggle, boolean defaultCollapsed, boolean animate) {
        collapser = new Collapser(collapserBuilder, defaultCollapsed);
        animateCollapser = animate;

        defaults().growX();

        table(header -> {
            headerBuilder.get(header);

            header.button(Icon.downOpen, Styles.emptyi, () -> {
                        collapser.toggle(animateCollapser);
                        onCollapserToggle.get(collapser.isCollapsed());
                    })
                    .update(i -> i.getStyle().imageUp = (!collapser.isCollapsed() ? Icon.upOpen : Icon.downOpen)).size(40f).right();
        });
        row();

        add(collapser);
    }
}
