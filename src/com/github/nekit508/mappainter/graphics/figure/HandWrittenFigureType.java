package com.github.nekit508.mappainter.graphics.figure;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.input.KeyCode;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.scene.event.InputEvent;
import arc.scene.ui.Slider;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.io.Reads;
import arc.util.io.Writes;
import com.github.nekit508.mappainter.graphics.Drawe;
import com.github.nekit508.mindustry.annotations.ioproc.IOProc;
import mindustry.Vars;
import mindustry.ui.Styles;

public class HandWrittenFigureType extends FigureType {
    public HandWrittenFigureType(String name) {
        super(name);
    }

    public class HandWrittenFigure extends Figure {
        @IOProc.IOField
        public float minx = Vars.world.unitWidth(), miny = Vars.world.unitHeight(), maxx = 0, maxy = 0;
        @IOProc.IOField
        public Seq<Vec2> points = new Seq<>();

        @IOProc.IOField
        public Color linesColor = Color.red;
        @IOProc.IOField
        public float linesStroke = 5;

        @Override
        public void constructCreationTable(Table infoTable) {
            infoTable.defaults().grow().uniform().center();

            infoTable.label(() -> "points: " + points.size).left();
            infoTable.add(new Slider(1, 64, 1, false, Styles.defaultSlider)).with(slider -> slider.moved(value -> linesStroke = value)).tooltip("stroke");
        }

        @Override
        public void drawCreation() {
            draw();
        }

        @Override
        public void draw() {
            if (points.size < 1) return;

            Draw.color(linesColor);
            Lines.stroke(linesStroke);

            for (int i = 1; i < points.size; i++) {
                Vec2 prev = points.get(i-1);
                Vec2 cur = points.get(i);

                if (cur == null || prev == null)
                    continue;

                Drawe.line(prev, cur);
            }
        }
        @Override
        public void hitbox(Rect out) {
            out.set(minx, miny, maxx - minx, maxy - miny);
        }

        @Override
        public void touchDragged(InputEvent event, float x, float y, int pointer) {
            Vec2 coords = localToWorldCoords(event, x, y);

            if (coords.x > maxx)
                maxx = coords.x;
            if (coords.x < minx)
                minx = coords.x;

            if (coords.y > maxy)
                maxy = coords.y;
            if (coords.y < miny)
                miny = coords.y;

            points.add(coords.cpy());
        }

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
            return button == KeyCode.mouseLeft;
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button) {
            points.add((Vec2) null);
        }

        @Override
        public void write(Writes writes) {
            super.write(writes);
        }

        @Override
        public void read(Reads reads) {
            super.read(reads);
        }
    }
}
