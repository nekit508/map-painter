package com.github.nekit508.mappainter.map.generator.states;

import com.github.nekit508.mappainter.map.generator.StateType;
import com.github.nekit508.mappainter.map.generator.WFCBaseGenerator;
import com.github.nekit508.mappainter.map.generator.WFCTile;
import mindustry.world.Block;

public class BuildingStateType extends StateType {
    public BuildingStateType(String name) {
        super(name);
    }

    public BuildingState place(WFCBaseGenerator generator, WFCTile tile, Block block) {
        return create(generator, tile, b -> b.block = block);
    }

    public class BuildingState extends State {
        public Block block;
        public boolean worked = false;

        @Override
        public void init() {
            super.init();

            for (int dx = -block.size / 2; dx < block.size / 2 + (block.size % 2); dx++) {
                for (int dy = -block.size / 2; dy < block.size / 2 + (block.size % 2); dy++) {
                    var t = tile.nearby(dx, dy);
                    t.state = this;
                }
            }
        }

        @Override
        public void step() {
            super.step();
            tile.world().setBlock(block, generator.team);
            worked = true;
        }

        @Override
        public boolean isActive() {
            return !worked;
        }

        @Override
        public boolean forDelete() {
            return worked;
        }
    }
}
