package com.github.nekit508.mappainter.graphics.figure;

import arc.Core;
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
import mindustry.Vars;
import mindustry.ui.Styles;

public class HandWrittenFigureType extends FigureType {
    public HandWrittenFigureType(String name) {
        super(name);
    }

    public class HandWrittenFigure extends Figure {
        public float minx = Vars.world.unitWidth(), miny = Vars.world.unitHeight(), maxx = 0, maxy = 0;
        public Seq<Vec2> points = new Seq<>();

        public Color linesColor = Color.red;
        public float linesStroke = 5;

        @Override
        public void onCreate(Table infoTable) {
            infoTable.defaults().grow().uniform().center();

            infoTable.label(() -> "points: " + points.size).left();
            infoTable.add(new Slider(1, 64, 1, false, Styles.defaultSlider)).with(slider -> slider.moved(value -> linesStroke = value)).tooltip("stroke");
        }

        @Override
        public void updateCreation(Table table) {

        }

        @Override
        public void drawCreation() {
            draw();
        }

        @Override
        public boolean created() {
            return false;
        }

        @Override
        public void draw() {
            if (points.size < 1) return;

            Draw.color(linesColor);
            Lines.stroke(linesStroke);

            for (int i = 1; i < points.size; i++) {
                Vec2 prev = points.get(i-1);
                Vec2 cur = points.get(i);

                if (cur == null) {
                    i++;
                    continue;
                }

                if (prev != null)
                    Drawe.line(prev, cur);
            }
        }

        @Override
        public void read(Reads reads) {
            
        }

        @Override
        public void write(Writes writes) {
            
        }

        @Override
        public void hitbox(Rect out) {
            out.set(minx, miny, maxx - minx, maxy - miny);
        }

        @Override
        public void touchDragged(InputEvent event, float x, float y, int pointer) {
            Vec2 coords = Core.scene.stageToScreenCoordinates(event.listenerActor.localToStageCoordinates(new Vec2(x, y)));
            coords.y = Core.scene.getHeight() - coords.y;
            coords = Core.camera.unproject(coords);

            if (coords.x > maxx)
                maxx = coords.x;
            if (coords.x < minx)
                minx = coords.x;

            if (coords.y > maxy)
                maxy = coords.y;
            if (coords.y < miny)
                miny = coords.y;

            points.add(coords);
        }

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
            return true;
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button) {
            points.add((Vec2) null);
        }
    }
}
