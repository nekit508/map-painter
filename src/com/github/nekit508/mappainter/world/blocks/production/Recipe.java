package com.github.nekit508.mappainter.world.blocks.production;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Building;

public abstract class Recipe<M extends Recipe.Memory<?>> {
    public final String name;

    public Recipe(String name) {
        this.name = name;
    }

    public abstract boolean canConsume();
    public abstract boolean shouldConsume();

    public abstract void updateConsumption();
    public abstract void consume();

    public abstract void dump();

    public abstract <B extends Building & MemoryProvider> M constructMemoryIn(B building);

    public void update() {
        if (canConsume()) {
            updateConsumption();
            if (shouldConsume())
                consume();
        }
    }


    public <B extends Building & MemoryProvider> M memoryOf(B building) {
        return (M) building.getMemoryOf(this);
    }

    public static abstract class Memory<B extends Building & MemoryProvider> {
        public B attachedBuilding;

        public Memory(B attachedBuilding) {
            this.attachedBuilding = attachedBuilding;
        }

        public B attachedBuilding() {
            return attachedBuilding;
        }

        public abstract void read(Reads reads);
        public abstract void write(Writes writes);
    }

    public interface MemoryProvider {
        <M extends Memory<?>> Memory<?> getMemoryOf(Recipe<M> recipe);
        <M extends Memory<?>> void registerFor(M memory, Recipe<M> recipe);
    }
}
