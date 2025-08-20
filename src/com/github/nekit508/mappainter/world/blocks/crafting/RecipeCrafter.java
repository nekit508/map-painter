package com.github.nekit508.mappainter.world.blocks.crafting;

import arc.func.Cons2;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.ui.Styles;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.meta.Stats;

public class RecipeCrafter extends GenericCrafter {
    public final Seq<Recipe> recipes = new Seq<>();

    public RecipeCrafter(String name) {
        super(name);

        config(String.class, (Cons2<RecipeCrafterBuilding, String>) (building, recipeName) -> {
            building.setRecipe(recipes.find(r -> r.name.equals(recipeName)));
        });

        configurable = true;
    }

    @Override
    public void setStats() {
        super.setStats();

        for (Recipe recipe : recipes)
            recipe.setStats(stats);
    }

    public void recipe(Recipe... recipes) {
        this.recipes.addAll(recipes);
    }

    public class RecipeCrafterBuilding extends GenericCrafterBuild {
        @Override
        public void buildConfiguration(Table table) {
            table.defaults().expandX().height(32);
            for (Recipe recipe : recipes) {
                table.button(recipe.name, Styles.cleart, () -> {
                    configure(recipe.name);
                });
            }
        }

        protected void setRecipe(Recipe recipe) {

        }
    }

    public static class Recipe {

        public static final ObjectMap<String, Recipe> all = new ObjectMap<>();
        public final Seq<LiquidStack> inputLiquids = new Seq<>(), outputLiquids = new Seq<>();
        public final Seq<ItemStack> inputItems = new Seq<>(), outputItems = new Seq<>();

        public final String name;

        public float powerInput, powerOutput;

        public Recipe(String name) {
            this.name = name;
            all.put(name, this);
        }

        public void setStats(Stats stats) {

        }
    }
}
