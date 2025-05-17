package com.github.nekit508.mappainter.ui.scene;

import arc.Core;
import arc.func.Cons;
import arc.graphics.g2d.ScissorStack;
import arc.math.Mathf;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.scene.event.ElementGestureListener;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.event.Touchable;
import arc.scene.ui.layout.Table;

public class ViewPane extends Table {
    private Table movable;

    public boolean clip = true;
    protected final Rect clipBounds = new Rect();
    protected final Rect scissorBounds = new Rect();

    public float minZoom = 0.3f, maxZoom = 3f, zoomScale = 0.1f;

    private Vec2 movableTranslation = new Vec2();

    public ViewPane(Cons<Table> tableCons) {
        touchable = Touchable.enabled;
        setLayoutEnabled(false);

        Table table = new Table();
        movable = table;
        tableCons.get(table);
        addChild(table);

        movable.setTransform(true);
        movable.setScale(1, 1);
        movable.translation.set(0, 0);

        addListener(new ElementGestureListener(){
            @Override
            public void pan(InputEvent event, float x, float y, float deltaX, float deltaY) {
                translate(deltaX, deltaY);
            }

            @Override
            public void zoom(InputEvent event, float initialDistance, float distance) {
                float scale = Mathf.clamp(movable.scaleX - (initialDistance / distance) * zoomScale, minZoom, maxZoom);
                movable.setScale(scale, scale);
            }
        });

        addCaptureListener(new InputListener(){
            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
                if (amountY != 0) {
                    float scale = Mathf.clamp(movable.scaleX - amountY * zoomScale, minZoom, maxZoom);
                    movable.setScale(scale, scale);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (Core.scene.hit(Core.input.mouseX(), Core.input.mouseY(), true) == this)
            Core.scene.setScrollFocus(this);
        else if (Core.scene.getScrollFocus() == this)
            Core.scene.setScrollFocus(null);
    }

    @Override
    public void draw() {
        if(clip) {
            clipBounds.set(x, y, getWidth(), getHeight());
            Core.scene.calculateScissors(clipBounds, scissorBounds);
            if (ScissorStack.push(scissorBounds)) {
                super.draw();
                ScissorStack.pop();
            }
        }
    }

    public Table getMovable() {
        return movable;
    }

    public void setMovable(Table movable) {
        this.movable = movable;
    }

    public void translate(float dx, float dy) {
        movableTranslation.add(dx, dy);
        movable.translation.add(dx, dy);
    }

    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        movable.translation.set(movableTranslation.x + width / 2, movableTranslation.y + height / 2);
    }
}
