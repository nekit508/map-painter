package com.github.nekit508.mappainter.world.consumers;

import com.github.nekit508.mappainter.world.blocks.energy.modulereactor.ModuleReactor;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.consumers.Consume;

public class ModuleReactorConsume extends Consume {
    public ModuleReactor.ModuleReactorBuild as(Building building) {
        return (ModuleReactor.ModuleReactorBuild) building;
    }

    public ModuleReactor as(Block block) {
        return (ModuleReactor) block;
    }
}
