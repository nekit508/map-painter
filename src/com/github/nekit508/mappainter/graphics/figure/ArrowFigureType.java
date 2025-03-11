package com.github.nekit508.mappainter.graphics.figure;

import arc.graphics.g2d.Lines;
import arc.math.Mathf;

public class ArrowFigureType extends LineFigureType {
    public float arrowLen = 30;

    public ArrowFigureType(String name) {
        super(name);
    }

    public class ArrowFigure extends LineFigure {
        @Override
        public void draw() {
            super.draw();
            float angle = tmpVec.set(end).sub(start).angle();

            Lines.line(end.x, end.y, end.x + Mathf.cosDeg(angle + 120) * arrowLen, end.y + Mathf.sinDeg(angle + 120) * arrowLen);
            Lines.line(end.x, end.y, end.x + Mathf.cosDeg(angle - 120) * arrowLen, end.y + Mathf.sinDeg(angle - 120) * arrowLen);
        }
    }
}
