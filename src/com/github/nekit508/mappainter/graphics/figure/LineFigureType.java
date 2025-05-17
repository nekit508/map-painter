package com.github.nekit508.mappainter.graphics.figure;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.input.KeyCode;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.scene.event.InputEvent;
import arc.util.io.Reads;
import arc.util.io.Writes;
import com.github.nekit508.mindustry.annotations.ioproc.IOProc;

public class LineFigureType extends FigureType {
    public LineFigureType(String name) {
        super(name);
    }

    public class LineFigure extends Figure {
        public Vec2 start = new Vec2(-1, -1), end = new Vec2(-1, -1);

        @Override
        public void drawCreation() {
            if (start.x == -1 || end.x == -1)
                return;

            draw();
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

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
            if (button != KeyCode.mouseLeft)
                return false;

            Vec2 coords = localToWorldCoords(event, x, y);
            start.set(coords.x, coords.y);
            end.set(-1, -1);
            return true;
        }

        @Override
        public void touchDragged(InputEvent event, float x, float y, int pointer) {
            Vec2 coords = localToWorldCoords(event, x, y);
            end.set(coords.x, coords.y);
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button) {
            if (button != KeyCode.mouseLeft)
                return;

            if (end.x == -1)
                start.set(-1, -1);
        }
    }
}

