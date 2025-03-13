package com.github.nekit508.mappainter.ui;

import arc.Core;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.event.Touchable;
import arc.scene.style.Drawable;
import arc.struct.Seq;
import com.github.nekit508.mappainter.graphics.g2d.MPDraw;

public class RadialMenu extends Element {
    public float drawStep = 6;
    public float rInner = 100, rOuter = 150;
    public float iconSize = 32;
    public TextureRegion reg;

    public KeyCode targetKeycode = KeyCode.mouseLeft;

    public RadialMenuButton centerButton;
    public Seq<RadialMenuButton> buttons = new Seq<>();
    public Cons<RadialMenu> buttonsConstructor;
    public Cons<RadialMenu> hidden, shown;

    public boolean ignoreInput = false;

    public Vec2 tmpVec2 = new Vec2();
    public Color tmpColor = new Color();

    protected RadialMenuButton lastMouseOver = null;

    public RadialMenu(Cons<RadialMenu> buttonsConstructor) {
        this.buttonsConstructor = buttonsConstructor;
        setLayoutEnabled(false);

        touchable = Touchable.enabled;

        addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
                if (targetKeycode != button || ignoreInput) return false;

                RadialMenuButton btn = getSelectedButton(tmpVec2.set(x, y));
                if (btn == null) return false;

                btn.clicked = true;

                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button) {
                if (targetKeycode == button || ignoreInput) {
                    RadialMenuButton btn = getSelectedButton(tmpVec2.set(x, y));
                    btn.clicked = false;
                    btn.clicked();
                    if (btn.hideOnClick)
                        hide();
                }
            }
        });
    }

    @Override
    public void draw() {
        super.draw();

        if (centerButton != null) {
            Draw.color(centerButton.getColor());
            Core.atlas.getDrawable("circle").draw(
                    x - rInner * scaleX,
                    y - rInner * scaleY,
                    rInner * 2 * scaleX,
                    rInner * 2 * scaleY
            );
            centerButton.icon.draw(
                    iconSize / -2 * scaleX + x,
                    iconSize / -2 * scaleY + y,
                    iconSize * scaleX,
                    iconSize * scaleY
            );
            Draw.color();
        }

        if (buttons.isEmpty())
            drawSegment(0, 360, tmpColor.set(0, 0, 0, 0.8f), null);
        else {
            float segmentSize = 360f / buttons.size;

            for (int i = 0; i < buttons.size; i++) {
                RadialMenuButton btn = buttons.get(i);
                tmpColor.set(btn.getColor());
                drawSegment(segmentSize * i, segmentSize * i + segmentSize, tmpColor, btn.icon);
            }
        }
    }

    @Override
    public void clear() {
        clearActions();
        buttons.clear();
    }

    public void center(RadialMenuButton button) {
        centerButton = button;
        button.parent = this;
    }

    public void addButton(RadialMenuButton button) {
        button.parent = this;
        buttons.add(button);
    }

    public void addButtons(Iterable<RadialMenuButton> buttons) {
        for (RadialMenuButton button : buttons)
            addButton(button);
    }

    public void addButtons(RadialMenuButton... buttons) {
        for (RadialMenuButton button : buttons)
            addButton(button);
    }

    public void show() {
        visible = true;
        ignoreInput = false;
        if (shown != null)
            shown.get(this);
    }

    /** Only rebuilds buttons. */
    public void rebuild() {
        buttons.clear();
        buttonsConstructor.get(this);
    }

    public void hide() {
        ignoreInput = true;
        visible = false;

        if (hidden != null)
            hidden.get(this);
    }

    @Override
    public Element hit(float x, float y, boolean touchable) {
        RadialMenuButton btn = getSelectedButton(tmpVec2.set(x, y));

        if (lastMouseOver != null && lastMouseOver != btn)
            lastMouseOver.mouseOver = false;
        if (btn != null)
            btn.mouseOver = true;
        lastMouseOver = btn;

        return btn != null ? this : null;
    }

    public RadialMenuButton getSelectedButton(Vec2 pos) {
        float segmentSize = 360f / buttons.size;

        float d2 = pos.len2();

        if (!buttons.isEmpty() && d2 > rInner * rInner && d2 < rOuter * rOuter) {
            float angle = tmpVec2.angle();
            return buttons.get(Mathf.floor(angle / segmentSize));
        } else if (d2 < rInner * rInner) {
            return centerButton;
        } else
            return null;
    }

    public void drawSegment(float start, float end, Color color, Drawable icon) {
        drawSegment(start, end, drawStep, color, icon);
    }

    public void drawSegment(float start, float end, float step, Color color, Drawable icon) {
        for (float i = start; i < end; i += step)
            drawPart(i, step, color);
        drawPart(end - (end - start) % step, (end - start) % step, color);

        if (icon != null) {
            float iconAngle = start + (end - start) / 2;
            float iconR = rInner + (rOuter - rInner) / 2;
            icon.draw(
                    (Mathf.cosDeg(iconAngle) * iconR - iconSize / 2) * scaleX + x,
                    (Mathf.sinDeg(iconAngle) * iconR  - iconSize / 2) * scaleY + y,
                    iconSize * scaleX,
                    iconSize * scaleY
            );
        }
    }

    public void drawPart(float offset, float size, Color color) {
        if (reg == null)
            reg = Core.atlas.find("whiteui");

        float cos1 = Mathf.cosDeg(offset) * scaleX;
        float cos2 = Mathf.cosDeg(offset + size) * scaleX;
        float sin1 = Mathf.sinDeg(offset) * scaleY;
        float sin2 = Mathf.sinDeg(offset + size) * scaleY;

        MPDraw.shapedRect(reg,
                cos1 * rInner + x, sin1 * rInner + y, color,
                cos1 * rOuter + x, sin1 * rOuter + y, color,
                cos2 * rOuter + x, sin2 * rOuter + y, color,
                cos2 * rInner + x, sin2 * rInner + y, color
        );
        Draw.color();

    }

    public static class RadialMenuButton {
        public RadialMenu parent;

        public Drawable icon;
        /** When mouse was over and touched up. */
        public Cons<RadialMenuButton> listener;
        public boolean active = true;
        public boolean checked = false;
        /** If mouse is over. */
        public boolean mouseOver = false;
        /** If mouse is over and pressed. */
        public boolean clicked = false;

        public boolean hideOnClick = false;

        Color tmpColor = new Color();
        public Color getColor() {
            if (!active)
                tmpColor.set(0, 0, 0, 0.9f);
            else if (mouseOver) {
                if (clicked)
                    tmpColor.set(0.3f, 0.3f, 0.3f, 0.9f);
                else
                    tmpColor.set(0.2f, 0.2f, 0.2f, 0.7f);
            }
            else
                tmpColor.set(0, 0, 0, 0.7f);

            if (checked)
                tmpColor.add(0.3f, 0.3f, 0.3f, 0.3f);

            return tmpColor;
        }

        public void clicked() {
            listener.get(this);
        }
    }
}
