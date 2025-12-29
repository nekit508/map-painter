package com.github.nekit508.mappainter.map.generator;

import mindustry.Vars;
import mindustry.world.Tile;

public class WFCTile {
    public int x, y;
    public TileState state = null;
    public WFCBaseGenerator generator;

    public WFCTile(int x, int y, WFCBaseGenerator generator) {
        this.x = x;
        this.y = y;
        this.generator = generator;
    }

    public WFCTile nearby(int dx, int dy) {
        return generator.world.get(dx, dy);
    }

    public Tile world() {
        return Vars.world.tile(x, y);
    }

    public interface TileState {

    }
}
