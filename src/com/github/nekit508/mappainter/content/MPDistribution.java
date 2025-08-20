package com.github.nekit508.mappainter.content;

import com.github.nekit508.mappainter.register.BaseRegistries;
import com.github.nekit508.mappainter.world.blocks.distribuition.LiquidSortTank;
import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.type.ItemStack;

public class MPDistribution {
    public static void init() {
        BaseRegistries.blocks.register("sorted-tank", () -> new LiquidSortTank("sorted-tank"){{
            health = 120;
            size = 2;
            liquidCapacity = 80;

            requirements(Category.liquid, ItemStack.with(Items.lead, 40, Items.metaglass, 40, Items.silicon, 16));
        }});

        BaseRegistries.blocks.register("small-sorted-tank", () -> new LiquidSortTank("small-sorted-tank"){{
            health = 35;
            size = 1;
            liquidCapacity = 10;

            requirements(Category.liquid, ItemStack.with(Items.lead, 5, Items.metaglass, 5, Items.silicon, 2));
        }});
    }
}
