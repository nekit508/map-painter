package com.github.nekit508.mappainter.map.generator;

import arc.struct.SnapshotSeq;
import com.github.nekit508.mappainter.map.generator.states.BuildingStateType;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.Team;

public class WFCBaseGenerator {
    public SnapshotSeq<StateType.State> states = new SnapshotSeq<>();
    public WorldOverlay<WFCTile> world = new WorldOverlay<>();

    public Team team = Team.green;

    public BuildingStateType stateType = new BuildingStateType("name");

    public WFCBaseGenerator() {
        world.setSize(Vars.world.width(), Vars.world.height());
        world.fill((x, y) -> new WFCTile(x, y, this));

        stateType.place(this, world.get(10, 10), Blocks.coreShard);
    }

    public boolean step() {
        for (StateType.State state : states)
            if (state.isActive())
                state.step();

        states.begin();
        for (StateType.State state : states)
            if (state.forDelete())
                states.remove(state);
        states.end();

        return !states.isEmpty();
    }
}
