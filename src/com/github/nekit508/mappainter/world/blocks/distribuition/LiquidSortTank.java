package com.github.nekit508.mappainter.world.blocks.distribuition;

import arc.func.Cons2;
import arc.scene.ui.layout.Table;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.world.blocks.ItemSelection;
import mindustry.world.blocks.liquid.LiquidRouter;

public class LiquidSortTank extends LiquidRouter {
    public LiquidSortTank(String name) {
        super(name);

        configurable = true;

        config(Liquid.class, (Cons2<LiquidSortTankBuild, Liquid>) (building, liquid) -> building.sortedLiquid = liquid);
    }

    public class LiquidSortTankBuild extends LiquidRouterBuild {
        public Liquid sortedLiquid;

        @Override
        public void buildConfiguration(Table table) {
            ItemSelection.buildTable(table, Vars.content.liquids(), () -> sortedLiquid, this::configure);
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            return liquid == sortedLiquid && super.acceptLiquid(source, liquid);
        }

        @Override
        public void dumpLiquid(Liquid liquid) {
            this.dumpLiquid(liquid, 1.0F);
        }

        @Override
        public void dumpLiquid(Liquid liquid, float scaling, int outputDir) {
            int dump = this.cdump;
            if (!(this.liquids.get(liquid) <= 1.0E-4F)) {
                if (!Vars.net.client() && Vars.state.isCampaign() && this.team == Vars.state.rules.defaultTeam) {
                    liquid.unlock();
                }

                for(int i = 0; i < this.proximity.size; ++i) {
                    this.incrementDump(this.proximity.size);
                    Building other = this.proximity.get((i + dump) % this.proximity.size);
                    if (outputDir == -1 || (outputDir + this.rotation) % 4 == this.relativeTo(other)) {
                        other = other.getLiquidDestination(this, liquid);
                        if (other != null && other.block.hasLiquids && this.canDumpLiquid(other, liquid) && other.liquids != null) {
                            this.transferLiquid(other, liquids.get(liquid) / scaling, liquid);
                        }
                    }
                }
            }
        }
    }
}
