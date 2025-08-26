package com.github.nekit508.mappainter.map.generator.statetypes;

import com.github.nekit508.mappainter.map.generator.statetypes.internal.Balance;
import mindustry.type.Item;
import mindustry.world.Block;

public class CoreStateType extends BlockStateType {
    public CoreStateType(String name, Block block) {
        super(name, block);
    }

    public class CoreState extends State {
        public Balance input = new Balance();

        @Override
        public void fill() {
            if (step == 1)
                super.fill();
            else if (step == 2) {

            }
        }

        protected void createLineupFor(Item target) {

        }

        @Override
        public boolean active() {
            return step >= 1;
        }
    }
}
