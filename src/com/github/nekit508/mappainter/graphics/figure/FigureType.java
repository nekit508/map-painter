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
import arc.util.Disposable;
import arc.util.Structs;
import arc.util.io.Reads;
import arc.util.io.Writes;

import java.lang.reflect.Constructor;

public abstract class FigureType {
    public String name;
    public Prov<? extends Figure> figureProv;

    public Drawable icon;

    public FigureType(String name) {
        this.name = name;

        initFigure();
    }

    public <T extends Figure> T create() {
        T out = (T) figureProv.get();
        out.type = this;
        return out;
    }

    public void load() {
        icon = Core.atlas.getDrawable("map-painter-" + name);
    }

    public void constructSelectionButton(Button button) {
        button.image(icon).tooltip(name);
    }

    protected void initFigure(){
        try {
            Class<?> current = getClass();

            if (current.isAnonymousClass())
                current = current.getSuperclass();

            while(figureProv == null && FigureType.class.isAssignableFrom(current)){
                Class<?> type = Structs.find(current.getDeclaredClasses(), t -> Figure.class.isAssignableFrom(t) && !t.isInterface());
                if(type != null) {
                    Constructor<? extends Figure> cons = (Constructor<? extends Figure>) type.getDeclaredConstructor(type.getDeclaringClass());
                    figureProv = () -> {
                        try {
                            return cons.newInstance(this);
                        } catch(Exception e) {
                            throw new RuntimeException(e);
                        }
                    };
                }

                current = current.getSuperclass();
            }

        } catch(Throwable exception) {
            throw new RuntimeException(exception);
        }

        if(figureProv == null){
            throw new RuntimeException("Figure not found in " + getClass() + ".");
        }
    }

    /**
     * Object that know how to draw, read and write itself.
     */
    public abstract class Figure extends InputListener implements QuadTree.QuadTreeObject, Disposable {
        public FigureType type;

        public void constructCreationTable(Table infoTable) {}

        public void updateCreation(Table table) {}

        public void drawCreation() {}

        public void created() {}

        /** Should be called after creation and all params set. */
        public void init() {}

        public void draw() {}

        // TODO
        /** Draw figure on the minimap. */
        public void drawMinimap() {}

        public void read(Reads reads) {}

        public void write(Writes writes) {}

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
