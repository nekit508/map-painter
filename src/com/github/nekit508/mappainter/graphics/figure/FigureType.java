package com.github.nekit508.mappainter.graphics.figure;

import arc.Core;
import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.input.KeyCode;
import arc.math.geom.QuadTree;
import arc.math.geom.Vec2;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.style.Drawable;
import arc.scene.ui.Button;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.*;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Icon;
import mindustry.ui.Styles;

import java.lang.reflect.Constructor;

public abstract class FigureType {
    public static Seq<FigureType> figureTypes = new Seq<>();
    public static ObjectMap<String, FigureType> figureTypesMap = new ObjectMap<>();

    public String typeName;
    public Prov<? extends Figure> figureProv;

    public @Nullable Class<?> subclass;

    public FigureType(String name) {
        typeName = name;

        figureTypesMap.put(typeName, this);
        figureTypes.add(this);

        initFigure();
    }

    public <T extends Figure> T create() {
        T out = (T) figureProv.get();
        out.type = this;
        return out;
    }

    public void ConstructSelectionButton(Button button) {
        Log.infoList(Core.atlas.find("map-painter-" + typeName));
        button.image((Drawable) Core.atlas.getDrawable("map-painter-" + typeName)).tooltip(typeName);
    }

    protected void initFigure(){
        try{
            Class<?> current = getClass();

            if(current.isAnonymousClass()){
                current = current.getSuperclass();
            }

            subclass = current;

            while(figureProv == null && FigureType.class.isAssignableFrom(current)){
                Class<?> type = Structs.find(current.getDeclaredClasses(), t -> Figure.class.isAssignableFrom(t) && !t.isInterface());
                if(type != null) {
                    Constructor<? extends Figure> cons = (Constructor<? extends Figure>) type.getDeclaredConstructor(type.getDeclaringClass());
                    figureProv = () -> {
                        try{
                            return cons.newInstance(this);
                        }catch(Exception e){
                            throw new RuntimeException(e);
                        }
                    };
                }

                current = current.getSuperclass();
            }

        }catch(Throwable ignored){
        }

        if(figureProv == null){
            throw new RuntimeException("Figure not found.");
        }
    }

    /**
     * Object that know how to draw, read and write itself.
     */
    public abstract class Figure extends InputListener implements QuadTree.QuadTreeObject, Disposable {
        public FigureType type;

        public abstract void onCreate(Table infoTable);

        public abstract void updateCreation(Table table);

        public abstract void drawCreation();

        public abstract boolean created();

        /** Should be called after creation and all params set. */
        public void init() {}

        public abstract void draw();

        // TODO
        /** Draw figure on the minimap. */
        public void drawMinimap() {}

        public abstract void read(Reads reads);

        public abstract void write(Writes writes);

        @Override
        public void dispose() {}
    }

    // TODO
    public static class Point extends Table {
        public Vec2 position = new Vec2();
        public String name;

        public Mover mover;

        public Point(String n) {
            name = n;
            addCaptureListener(mover = new Mover());
            image((Drawable) Core.atlas.getDrawable("circle")).fill();
        }

        public void build(Table table) {
            table.button(name, Icon.add, Styles.flatt, () -> {

            }).growX().height(32);
        }

        public void draw() {
            Draw.color(Color.red);
            Fill.circle(position.x, position.y, 2.5f);
        }

        public void addToMap(Table mapOverlay) {
            mapOverlay.add(this);
        }

        public class Mover extends InputListener {
            public float lastX, lastY;

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
                Point.this.toFront();

                Vec2 pos = event.listenerActor.localToStageCoordinates(Tmp.v1.set(x, y));
                lastX = pos.x;
                lastY = pos.y;
                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer){
                Vec2 pos = event.listenerActor.localToStageCoordinates(Tmp.v1.set(x, y));

                Point.this.moveBy(pos.x - lastX, pos.y - lastY);
                lastX = pos.x;
                lastY = pos.y;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button){

            }
        }
    }
}
