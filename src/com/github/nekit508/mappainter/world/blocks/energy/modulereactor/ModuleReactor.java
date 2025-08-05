package com.github.nekit508.mappainter.world.blocks.energy.modulereactor;

import arc.struct.EnumSet;
import mindustry.world.blocks.power.PowerGenerator;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.Env;

public class ModuleReactor extends PowerGenerator {
    public float maxCasingHeat = 0;

    public ModuleReactor(String name) {
        super(name);

        itemCapacity = 30;
        liquidCapacity = 30;
        hasItems = true;
        hasLiquids = true;
        rebuildable = false;
        flags = EnumSet.of(BlockFlag.reactor, BlockFlag.generator);
        schematicPriority = -6;
        envEnabled = Env.any;
    }

    public class ModuleReactorBuild extends GeneratorBuild {
        /**
         * [0;1] <br>
         * 0 means that rods is fully in reactor.
         */
        public float targetControlRodPos = 0;

        public float casingHeat = 0;



        @Override
        public void updateTile() {


            if (casingHeat > maxCasingHeat)
                coreMeltdown();
        }

        @Override
        public void createExplosion() {

        }

        public void coreMeltdown() {
            kill();
        }
    }
}
