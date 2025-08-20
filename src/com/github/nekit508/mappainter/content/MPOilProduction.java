package com.github.nekit508.mappainter.content;

import arc.graphics.Color;
import arc.struct.Seq;
import com.github.nekit508.mappainter.register.BaseRegistries;
import com.github.nekit508.mappainter.ui.scene.Pie;
import com.github.nekit508.mappainter.ui.stats.MPStats;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.content.StatusEffects;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.type.LiquidStack;
import mindustry.world.blocks.production.GenericCrafter;

public class MPOilProduction {
    public static void init() {
        var crudeLightOil = BaseRegistries.liquids.register("crude-light-oil", () -> new Liquid("crude-light-oil", Color.rgb(41, 18, 0)){{
            viscosity = 0.8f;
            flammability = 0.2f;
            explosiveness = 0.2f;
            heatCapacity = 0.2f;
            barColor = color;
            effect = StatusEffects.tarred;
            boilPoint = 0.8f;
            gasColor = color;
            canStayOn.add(Liquids.water);
        }});

        var petrolGas = BaseRegistries.liquids.register("petrol-gas", () -> new Liquid("petrol-gas", Color.rgb(20, 82, 80)){{
            viscosity = 0.1f;
            flammability = 2.5f;
            explosiveness = 2.3f;
            heatCapacity = 0.1f;
            barColor = color;
            effect = StatusEffects.tarred;
            boilPoint = 0.2f;
            gasColor = color;
            canStayOn.add(Liquids.water);
        }});

        var petrol = BaseRegistries.liquids.register("petrol", () -> new Liquid("petrol", Color.rgb(168, 139, 0)){{
            viscosity = 0.2f;
            flammability = 1.5f;
            explosiveness = 1.0f;
            heatCapacity = 0.2f;
            barColor = color;
            effect = StatusEffects.tarred;
            boilPoint = 0.3f;
            gasColor = color;
            canStayOn.add(Liquids.water);
        }});

        var naphtha = BaseRegistries.liquids.register("naphtha", () -> new Liquid("naphtha", Color.rgb(168, 95, 0)){{
            viscosity = 0.3f;
            flammability = 0.5f;
            explosiveness = 0.3f;
            heatCapacity = 0.4f;
            barColor = color;
            effect = StatusEffects.tarred;
            boilPoint = 0.5f;
            gasColor = color;
            canStayOn.add(Liquids.water);
        }});

        var kerosene = BaseRegistries.liquids.register("kerosene", () -> new Liquid("kerosene", Color.rgb(0, 168, 160)){{
            viscosity = 0.3f;
            flammability = 0.4f;
            explosiveness = 0.3f;
            heatCapacity = 0.3f;
            barColor = color;
            effect = StatusEffects.tarred;
            boilPoint = 0.4f;
            gasColor = color;
            canStayOn.add(Liquids.water);
        }});

        var diesel = BaseRegistries.liquids.register("diesel", () -> new Liquid("diesel", Color.rgb(66, 13, 0)){{
            viscosity = 0.5f;
            flammability = 0.0f;
            explosiveness = 0.2f;
            heatCapacity = 0.5f;
            barColor = color;
            effect = StatusEffects.tarred;
            boilPoint = 0.6f;
            gasColor = color;
            canStayOn.add(Liquids.water);
        }});

        BaseRegistries.blocks.register("oil-refinery", () -> new GenericCrafter("oil-refinery"){{
            health = 200;
            size = 3;
            liquidCapacity = 80;

            consumesPower = true;
            hasItems = true;
            hasPower = true;
            hasLiquids = true;
            outputsLiquid = true;

            dumpExtraLiquid = false;

            consumeLiquid(crudeLightOil.get(), 100f / 60f);
            consumePower(60f / 60f);

            outputLiquids = LiquidStack.with(
                    petrolGas.get(), 30 / 60f,
                    petrol.get(), 12.5 / 60f,
                    naphtha.get(), 7.5 / 60f,
                    kerosene.get(), 7.5 / 60f,
                    diesel.get(), 2.5 / 60f
            );

            requirements(Category.crafting, ItemStack.with(Items.lead, 100, Items.silicon, 50, Items.metaglass, 80, Items.titanium, 50));
        }

            @Override
            public void setStats() {
                super.setStats();
                stats.add(MPStats.refineryPie, table -> {
                    table.table(legend -> {
                        for (LiquidStack stack : outputLiquids) {
                            legend.image().color(stack.liquid.color).size(16).padRight(10);
                            legend.label(() -> stack.liquid.localizedName).expandX().left().row();
                        }
                    }).right();

                    table.spacerX(() -> 50);

                    var providers = new Seq<Pie.Provider>();
                    for (LiquidStack stack : outputLiquids)
                        providers.add(new Pie.Provider(() -> stack.liquid.color, () -> stack.amount, () -> null));
                    table.add(new Pie(providers, 60)).expand().left();
                });
            }
        });
    }
}
