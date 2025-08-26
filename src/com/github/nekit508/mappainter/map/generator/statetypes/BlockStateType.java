package com.github.nekit508.mappainter.map.generator.statetypes;

import com.github.nekit508.mappainter.map.generator.StateType;
import com.github.nekit508.mappainter.map.generator.WFCBaseGenerator;
import mindustry.content.Blocks;
import mindustry.world.Block;

public class BlockStateType extends StateType {
    public Block block;

    public BlockStateType(String name, Block block) {
        super(name);
        this.block = block;
    }

    @Override
    public boolean canCreateAt(WFCBaseGenerator generator, int x, int y) {
        int offset = -(block.size - 1) / 2;
        for (int dx = 0; dx < block.size; dx++) {
            for (int dy = 0; dy < block.size; dy++) {
                int wx = dx + offset + x;
                int wy = dy + offset + y;
                if (generator.stateAt(wx, wy) != null || generator.tileAt(wx, wy).block() != Blocks.air)
                    return false;
            }
        }
        return true;
    }

    public class BlockState extends State {
        @Override
        public void init() {
            super.init();
            int offset = -(block.size - 1) / 2;
            for (int dx = 0; dx < block.size; dx++) {
                for (int dy = 0; dy < block.size; dy++) {
                    int wx = dx + offset + x;
                    int wy = dy + offset + y;
                    generator.stateAt(wx, wy, this);
                }
            }
        }

        @Override
        public void fill() {
            super.fill();
            generator.tileAt(x, y).setBlock(block, generator.team());
        }
    }
}
