package com.github.nekit508.mappainter.graphics;

import arc.Core;
import arc.Events;
import arc.graphics.g2d.Draw;
import arc.math.geom.QuadTree;
import arc.math.geom.Rect;
import arc.struct.Seq;
import arc.util.Disposable;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import com.github.nekit508.mappainter.core.MPCore;
import com.github.nekit508.mappainter.graphics.figure.FigureType;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.graphics.Layer;

public class MPRenderer {
    public QuadTree<FigureType.Figure> figuresTree;

    public MPRenderer() {
        Events.on(EventType.WorldLoadEvent.class, e -> {
            if (figuresSeq != null)
                figuresSeq.each(Disposable::dispose);

            figuresTree = new QuadTree<>(new Rect(0, 0, Vars.world.unitWidth(), Vars.world.unitHeight()));
            figuresSeq = new Seq<>();
        });

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

        Rect bounds = Core.camera.bounds(Tmp.r3).grow(Vars.tilesize * 2f);

        figuresTree.intersect(bounds, FigureType.Figure::draw);

        Draw.reset();
    }

    // TODO
    public void drawMinimap() {
        Draw.reset();

        figuresSeq.each(FigureType.Figure::drawMinimap);

        Draw.reset();
    }

    public void write(Writes writes) {
        writes.i(figuresSeq.size);
        figuresSeq.each(f -> {
            writes.str(f.type.typeName);

            f.write(writes);
        });
    }

    public void read(Reads reads) {
        int size = reads.i();
        for (int i = 0; i < size; i++) {
            FigureType.Figure figure = FigureType.figureTypesMap.get(reads.str()).create();
            figuresSeq.add(figure);

            figure.read(reads);
        }
    }

    public void add(FigureType.Figure figure) {
        figuresSeq.add(figure);
        figuresTree.insert(figure);
    }

    public void remove(FigureType.Figure figure) {
        figuresSeq.remove(figure);
        figuresTree.remove(figure);
    }
}
