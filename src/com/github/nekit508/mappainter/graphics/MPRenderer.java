package com.github.nekit508.mappainter.graphics;

import arc.Events;
import arc.graphics.g2d.Draw;
import com.github.nekit508.mappainter.core.MPCore;
import com.github.nekit508.mappainter.graphics.figure.FigureType;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.graphics.Layer;

public class MPRenderer {

    public MPRenderer() {
        Events.run(EventType.Trigger.drawOver, () -> {
            if (Vars.state.isGame())
                draw();
        });
    }

    public void draw() {
        Draw.reset();
        Draw.z(Layer.fogOfWar + 5);

        if (MPCore.control.figure != null)
            MPCore.control.figure.drawCreation();

        MPCore.figuresManager.figures.each(FigureType.Figure::draw);

        Draw.reset();
    }

    // TODO
    public void drawMinimap() {
        Draw.reset();

        MPCore.figuresManager.figures.each(FigureType.Figure::drawMinimap);

        Draw.reset();
    }
}
