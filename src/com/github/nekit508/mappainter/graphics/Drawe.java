package com.github.nekit508.mappainter.graphics;

import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.geom.Vec2;

public class Drawe {
    public static void line(Vec2 start, Vec2 end) {
        float r = Lines.getStroke() / 2;

        Fill.circle(start.x, start.y, r);
        Lines.line(start.x, start.y, end.x, end.y, true);
        Fill.circle(end.x, end.y, r);
    }
}
