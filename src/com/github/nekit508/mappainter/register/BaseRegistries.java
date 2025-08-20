package com.github.nekit508.mappainter.register;

import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.type.Planet;
import mindustry.type.SectorPreset;
import mindustry.world.Block;

public final class BaseRegistries {
    public static Registry<Item> items = new Registry<>("items");
    public static Registry<Liquid> liquids = new Registry<>("liquids");
    public static Registry<Block> blocks = new Registry<>("blocks");
    public static Registry<SectorPreset> sectorPresets = new Registry<>("sector-resets");
    public static Registry<Planet> planets = new Registry<>("planets");
}
