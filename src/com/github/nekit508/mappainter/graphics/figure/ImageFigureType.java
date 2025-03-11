package com.github.nekit508.mappainter.graphics.figure;

import arc.math.geom.Rect;

public class ImageFigureType extends FigureType {
    public ImageFigureType(String name) {
        super(name);
    }

    public class ImageFigure extends Figure {
        @Override
        public void hitbox(Rect out) {
            out.set(0, 0, 0, 0);
        }
    }
}
