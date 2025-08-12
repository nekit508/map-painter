package com.github.nekit508.mappainter.content;

import com.github.nekit508.mappainter.world.fx.ExplosionEffect;

public class MPFx {
    public static ExplosionEffect explosionEffect;

    public static void load() {
        explosionEffect = new ExplosionEffect();
    }
}
