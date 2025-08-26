package com.github.nekit508.mappainter.map.generator;

import arc.func.Boolf;
import arc.math.geom.Point2;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import com.github.nekit508.mappainter.map.generator.statetypes.CoreStateType;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.Team;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;

// TODO
public class WFCBaseGenerator {
    protected ObjectMap<Floor, Point2> oreSources;

    /** Used for  */
    protected StateType.State[][] mapMask;

    protected Seq<StateType.State> activeStates;
    protected Seq<StateType.State> inactiveStates;

    protected CoreStateType core;

    protected Team team;

    public WFCBaseGenerator() {
        core = new CoreStateType("core", Blocks.coreShard);
    }

    public void generate(Team team) {
        this.team = team;
        oreSources = new ObjectMap<>();
        activeStates = new Seq<>();
        inactiveStates = new Seq<>();
        mapMask = new StateType.State[Vars.world.width()][Vars.world.height()];

        initialize();
        while (canStep())
            step();
        end();
    }

    protected void initialize() {
        placeCore();
    }

    protected void step() {
        for (StateType.State state : activeStates) {
            state.step();
        }

        activeStates.retainAll(state -> {
            if (!state.active()) {
                inactiveStates.add(state);
                return false;
            }
            return true;
        });

        inactiveStates.retainAll(state -> !state.forDelete());
    }

    protected boolean canStep() {
        return !activeStates.isEmpty();
    }

    protected void end() {
        fill();
    }

    protected void fill() {
        for (StateType.State state : inactiveStates) {
            state.fill();
        }
    }

    protected void placeCore() {
        var tile = findNearestTile(Vars.world.width() / 2, Vars.world.height() / 2, t -> core.canCreateAt(this, t.x, t.y), -1);
        if (tile != null)
            core.create(this, tile.x, tile.y);
        else
            Log.warn("Core was not placed.");
    }

    protected Tile findNearestTile(int x, int y, Boolf<Tile> validator, int maxDepth) {
        var root = tileAt(x, y);

        var tmp = new Seq<Tile>();
        var prevProcessed = new Seq<Tile>();
        var curProcessed = new Seq<Tile>();
        var forProcess = new Seq<Tile>();
        var nextForProcess = new Seq<Tile>();

        forProcess.add(root);
        while (!forProcess.isEmpty() && maxDepth != 0) {
            curProcessed.clear();
            for (Tile tile : forProcess) {
                curProcessed.add(tile);
                if (validator.get(tile))
                    return tile;

                nextForProcess.addAll(tile.getLinkedTiles(tmp).retainAll(t -> !prevProcessed.contains(t)));
            }

            prevProcessed.set(curProcessed);
            forProcess.set(nextForProcess);
            nextForProcess.clear();

            maxDepth--;
        }

        return null;
    }

    public Team team() {
        return team;
    }

    public Tile tileAt(int x, int y) {
        return Vars.world.tile(x, y);
    }

    public StateType.State stateAt(int x, int y) {
        return mapMask[x][y];
    }

    public void stateAt(int x, int y, StateType.State state) {
        mapMask[x][y] = state;
    }
}
