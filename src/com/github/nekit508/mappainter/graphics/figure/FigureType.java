package com.github.nekit508.mappainter.graphics.figure;

import arc.Core;
import arc.func.Prov;
import arc.math.geom.QuadTree;
import arc.math.geom.Vec2;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.style.Drawable;
import arc.scene.ui.Button;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Disposable;
import arc.util.Nullable;
import arc.util.Structs;
import arc.util.io.Reads;
import arc.util.io.Writes;

import java.lang.reflect.Constructor;

public abstract class FigureType {
    public static Seq<FigureType> figureTypes = new Seq<>();
    public static ObjectMap<String, FigureType> figureTypesMap = new ObjectMap<>();

    public String typeName;
    public Prov<? extends Figure> figureProv;

    public @Nullable Class<?> subclass;

    public Drawable icon;

    public FigureType(String name) {
        typeName = name;

        figureTypesMap.put(typeName, this);
        figureTypes.add(this);

        initFigure();

        load();
    }

    public <T extends Figure> T create() {
        T out = (T) figureProv.get();
        out.type = this;
        return out;
    }

    public void load() {
        icon = Core.atlas.getDrawable("map-painter-" + typeName);
    }

    public void constructSelectionButton(Button button) {
        button.image(icon).tooltip(typeName);
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

        public void constructCreationTable(Table infoTable) {};

        public void updateCreation(Table table) {};

        public void drawCreation() {};

        public void created() {}

        /** Should be called after creation and all params set. */
        public void init() {}

        public void draw() {};

        // TODO
        /** Draw figure on the minimap. */
        public void drawMinimap() {}

        public void read(Reads reads) {};

        public void write(Writes writes) {};

        @Override
        public void dispose() {}

        public Vec2 tmpVec = new Vec2();
        public Vec2 localToWorldCoords(InputEvent event, float x, float y) {
            Vec2 coords = Core.scene.stageToScreenCoordinates(event.listenerActor.localToStageCoordinates(tmpVec.set(x, y)));
            coords.y = Core.scene.getHeight() - coords.y;
            return Core.camera.unproject(coords);
        }
    }
}
