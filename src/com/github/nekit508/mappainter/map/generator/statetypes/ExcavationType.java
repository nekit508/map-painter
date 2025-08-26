package com.github.nekit508.mappainter.map.generator.statetypes;

import com.github.nekit508.mappainter.map.generator.StateType;
import com.github.nekit508.mappainter.map.generator.WFCBaseGenerator;
import com.github.nekit508.mappainter.map.generator.statetypes.internal.Balance;
import mindustry.content.Blocks;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.OreBlock;

public class ExcavationType extends StateType {
    public Balance target;

    public ExcavationType(Balance target, String name) {
        super(name);
        this.target = target;
    }

    @Override
    public boolean canCreateAt(WFCBaseGenerator generator, int x, int y) {
        return generator.tileAt(x, y).block() == Blocks.air && (generator.tileAt(x, y).overlay() instanceof OreBlock ore && false);
    }

    protected Balance calculateNumAt(Tile tile) {
        return null;
    }

    public class Excavation extends State {
        @Override
        public void fill() {
            super.fill();
            generator.tileAt(x, y).setBlock(Blocks.copperWall, generator.team());
        }
    }
}
