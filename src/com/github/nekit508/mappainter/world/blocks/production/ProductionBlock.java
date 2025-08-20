package com.github.nekit508.mappainter.world.blocks.production;

import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Building;
import mindustry.world.Block;

public class ProductionBlock extends Block {
    public ProductionBlock(String name) {
        super(name);
    }

    public Seq<Recipe<?>> getRecipes() {
        return Seq.with();
    }

    public class ProductionBuilding extends Building implements Recipe.MemoryProvider {
        public ObjectMap<Recipe<?>, Recipe.Memory<?>> memories = new ObjectMap<>();
        public Recipe<?> currentRecipe;

        @Override
        public boolean dump() {
            currentRecipe.dump();
            return super.dump();
        }

        @Override
        public void update() {
            currentRecipe.update();
            super.update();
        }

        public Recipe<?> currentRecipe() {
            return currentRecipe;
        }

        @Override
        public <M extends Recipe.Memory<?>> Recipe.Memory<?> getMemoryOf(Recipe<M> recipe) {
            return memories.get(recipe);
        }

        @Override
        public <M extends Recipe.Memory<?>> void registerFor(M memory, Recipe<M> recipe) {
            memories.put(recipe, memory);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);


        }

        @Override
        public void write(Writes write) {
            super.write(write);


        }
    }
}
