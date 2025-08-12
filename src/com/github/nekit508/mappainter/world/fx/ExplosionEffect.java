package com.github.nekit508.mappainter.world.fx;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Angles;
import mindustry.content.Fx;
import mindustry.entities.Effect;

public class ExplosionEffect extends Effect {
    public float radius = 80;
    public float fogRadius = 20;

    public ExplosionEffect() {
        super();
        renderer = e -> {
            Fx.rand.setSeed(e.id);

            for (int i = 0; i < Fx.rand.random(20, 30); i++) {
                int finalI = i;
                e.scaled(e.lifetime * Fx.rand.random(0.5f, 0.8f), b -> {
                    Fx.rand.setSeed(b.id + finalI);
                    var angle = Fx.rand.random(0, 360);
                    var l = Fx.rand.random(radius * 0.8f, radius * 1.2f) * b.fin() + Fx.rand.random(2f, 3f) * b.fin() * b.fin() / 2;
                    var r = Fx.rand.random(fogRadius * 0.8f, fogRadius * 1.2f) * b.fin();
                    var x = Angles.trnsx(angle, l);
                    var y = Angles.trnsy(angle, l);

                    Draw.color(fogColor(b.fout()));
                    Draw.alpha(1);
                    Fill.circle(b.x + x, b.y + y, r);
                });
            }

            Fx.rand.setSeed(e.id);
        };
        clip = 80;
        lifetime = 180f;
    }

    protected Color fogColor = new Color();
    public Color fogColor(float fout) {
        return fogColor.set(Color.red).a(1);
    }
}