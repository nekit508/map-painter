package com.github.nekit508.mappainter.graphics.figure;

import arc.math.geom.Rect;

public class CircleFigureType extends FigureType {
    public CircleFigureType(String name) {
        super(name);
    }

    public class CircleFigure extends Figure {
        @Override
        public void hitbox(Rect out) {
            out.set(0, 0, 0, 0);
        }
    }
}
