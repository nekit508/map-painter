package com.github.nekit508.mappainter.world.blocks.misc;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import com.github.nekit508.mappainter.world.blocks.NormalMapRenderer;
import mindustry.gen.Building;
import mindustry.world.Block;

public class NormalBlock extends Block {
    public TextureRegion normal;

    public NormalBlock(String name) {
        super(name);

        this.update = true;
        this.solid = true;
    }

    @Override
    public void load() {
        super.load();

        normal = Core.atlas.find(name + "-normal-map");
    }

    public class NormalBuilding extends Building implements NormalMapRenderer {
        @Override
        public void render() {
            Draw.rect(normal, x, y);
        }
    }
}
