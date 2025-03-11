package com.github.nekit508.mappainter.ui;

import arc.Core;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.Group;
import arc.scene.style.Drawable;
import arc.struct.Seq;
import com.github.nekit508.mappainter.graphics.g2d.MPDraw;

public class RadialMenu extends Group {
    public float drawStep = 6;
    public float rInner = 100, rOuter = 150;
    public float iconSize = 32;
    public TextureRegion reg;

    public KeyCode targetKeycode = KeyCode.mouseLeft;

    public Seq<RadialMenuButton> buttons = new Seq<>();
    public Cons<RadialMenu> buttonsConstructor;
    public Cons<RadialMenu> hidden, shown;

    public boolean hideOnSelect = false;
    public boolean ignoreInput = false;

    public Vec2 tmpVec2 = new Vec2();
    public Color tmpColor = new Color();

    public RadialMenu(Cons<RadialMenu> buttonsConstructor) {
        this.buttonsConstructor = buttonsConstructor;
        setLayoutEnabled(false);
    }

    RadialMenuButton selected;
    @Override
    public void draw() {
        super.draw();

        if (buttons.isEmpty())
            drawSegment(0, 360, tmpColor.set(0, 0, 0, 0.8f), null);
        else {
            float segmentSize = 360f / buttons.size;

            if (!ignoreInput)
                selected = getSelectedButton(Core.input.mouse());

            boolean exec = false;
            for (int i = 0; i < buttons.size; i++) {
                RadialMenuButton btn = buttons.get(i);

                if (!btn.active)
                    tmpColor.set(0, 0, 0, 0.9f);
                else if (btn == selected) {
                    if (Core.input.keyTap(targetKeycode)) {
                        tmpColor.set(0.3f, 0.3f, 0.3f, 0.9f);
                        exec = true;
                    } else
                        tmpColor.set(0.2f, 0.2f, 0.2f, 0.7f);
                }
                else
                    tmpColor.set(0, 0, 0, 0.7f);

                if (btn.checked)
                    tmpColor.add(0.2f, 0.2f, 0.2f);

                drawSegment(segmentSize * i, segmentSize * i + segmentSize, tmpColor, btn.icon);
            }

            if (exec) {
                selected.listener.get(selected);
                if (hideOnSelect)
                    hide();
            }
        }
    }

    @Override
    public void clear() {
        super.clear();
        buttons.clear();
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

    public void rebuild() {
        clear();
        buttonsConstructor.get(this);
    }

    public void hide() {
        ignoreInput = true;
        visible = false;

        if (hidden != null)
            hidden.get(this);
    }

    public RadialMenuButton getSelectedButton(Vec2 pos) {
        float segmentSize = 360f / buttons.size;

        float d2 = tmpVec2.set(pos).sub(x, y).len2();

        if (d2 > rInner * rInner && d2 < rOuter * rOuter) {
            float angle = tmpVec2.angle();
            return buttons.get(Mathf.floor(angle / segmentSize));
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

        Draw.color(color);
        MPDraw.shapedRect(reg,
                cos1 * rInner + x, sin1 * rInner + y,
                cos1 * rOuter + x, sin1 * rOuter + y,
                cos2 * rOuter + x, sin2 * rOuter + y,
                cos2 * rInner + x, sin2 * rInner + y
        );
        Draw.color();
    }

    public static class RadialMenuButton {
        public RadialMenu parent;

        public Drawable icon;
        public Cons<RadialMenuButton> listener;
        public boolean active = true;
        public boolean checked = false;
    }
}
