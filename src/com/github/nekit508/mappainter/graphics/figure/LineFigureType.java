package com.github.nekit508.mappainter.graphics.figure;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.input.KeyCode;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Table;
import arc.util.io.Reads;
import arc.util.io.Writes;

public class LineFigureType extends FigureType {
    public LineFigureType(String name) {
        super(name);
    }

    public class LineFigure extends Figure {
        public Vec2 start = new Vec2(-1, -1), end = new Vec2(-1, -1);
        public Point startp = new Point("start"), endp = new Point("end");

        @Override
        public void onCreate(Table infoTable) {
            startp.build(infoTable);

            infoTable.row();

            endp.build(infoTable);
        }

        @Override
        public void updateCreation(Table table) {
            if (Core.input.keyTap(KeyCode.mouseLeft)) {
                (start.x == -1 ? start : end).set(Core.input.mouseWorld());
            }
        }

        @Override
        public void drawCreation() {
            startp.draw();
            endp.draw();
        }

        @Override
        public boolean created() {
            return false;
        }

        @Override
        public void draw() {
            Lines.stroke(5);
            Draw.color(Color.red);
            Lines.line(start.x, start.y, end.x, end.y);
        }

        @Override
        public void read(Reads reads) {
            start.x = reads.f();
            start.y = reads.f();
            end.x = reads.f();
            end.y = reads.f();
        }

        @Override
        public void write(Writes writes) {
            writes.f(start.x);
            writes.f(start.y);
            writes.f(end.x);
            writes.f(end.y);
        }

        @Override
        public void hitbox(Rect rect) {
            float minX = Math.min(start.x, end.x);
            float maxX = Math.max(start.x, end.x);

            float minY = Math.min(start.y, end.y);
            float maxY = Math.max(start.y, end.y);

            rect.set(minX, minY, maxX - minX, maxY - minY);
        }
    }
}

