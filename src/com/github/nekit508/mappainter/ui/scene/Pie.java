package com.github.nekit508.mappainter.ui.scene;

import arc.Core;
import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.Element;
import arc.scene.style.Drawable;
import arc.struct.FloatSeq;
import arc.struct.Seq;
import com.github.bsideup.jabel.Desugar;
import com.github.nekit508.mappainter.graphics.g2d.MPDraw;

public class Pie extends Element {
    protected Seq<Provider> fractionProviders;
    protected float iconSize = 32;
    protected float drawStep = 6;
    protected float radius;

    public Pie(Seq<Provider> fractionProviders, float radius) {
        this.fractionProviders = fractionProviders;
        this.radius = radius;
    }

    @Override
    public float getPrefWidth() {
        return radius * 2f;
    }

    @Override
    public float getPrefHeight() {
        return radius * 2f;
    }

    @Override
    public void draw() {
        super.draw();

        var sum = 0f;
        var fractions = new FloatSeq();
        for (var provider : fractionProviders) {
            fractions.add(provider.fraction.get());
            sum += fractions.peek();
        }

        var start = 0f;
        for (int i = 0; i < fractionProviders.size; i++) {
            var provider = fractionProviders.get(i);
            float end = start + 360f * fractions.get(i) / sum;
            drawSegment(start, end, provider.color.get(), provider.drawable.get());
            start = end;
        }
    }

    public void drawSegment(float start, float end, Color color, Drawable icon) {
        drawSegment(start, end, drawStep, color, icon);
    }

    public void drawSegment(float start, float end, float step, Color color, Drawable icon) {
        for (float i = start; i < end; i += step)
            drawPart(i, step, color);
        drawPart(end - (end - start) % step, (end - start) % step, color);

        var hw = getWidth() / 2;
        var hh = getHeight() / 2;

        if (icon != null) {
            float iconAngle = start + (end - start) / 2;
            float iconR = radius * 1.5f;
            icon.draw(
                    (Mathf.cosDeg(iconAngle) * iconR - iconSize / 2) * scaleX + x + hw,
                    (Mathf.sinDeg(iconAngle) * iconR  - iconSize / 2) * scaleY + y + hh,
                    iconSize * scaleX,
                    iconSize * scaleY
            );
        }
    }

    protected TextureRegion drawPart$temp$reg;
    public void drawPart(float offset, float size, Color color) {
        if (drawPart$temp$reg == null)
            drawPart$temp$reg = Core.atlas.find("whiteui");

        float cos1 = Mathf.cosDeg(offset) * scaleX;
        float cos2 = Mathf.cosDeg(offset + size) * scaleX;
        float sin1 = Mathf.sinDeg(offset) * scaleY;
        float sin2 = Mathf.sinDeg(offset + size) * scaleY;

        var hw = getWidth() / 2;
        var hh = getHeight() / 2;

        MPDraw.shapedRect(drawPart$temp$reg,
                x + hw, y + hh, color,
                cos1 * radius + x + hw, sin1 * radius + y + hh, color,
                cos2 * radius + x + hw, sin2 * radius + y + hh, color,
                x + hw, y + hh, color
        );
    }

    @Desugar
    public record Provider(Prov<Color> color, Prov<Float> fraction, Prov<Drawable> drawable) {}
}
