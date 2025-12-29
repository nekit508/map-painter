package com.github.nekit508.mappainter.map.generator;

import arc.func.Cons;
import arc.func.Prov;
import arc.util.Structs;

import java.lang.reflect.Constructor;

public abstract class StateType {
    public String name;
    protected Prov<? extends State> stateType;

    public StateType(String name) {
        this.name = name;
        initState();
    }

    public <T extends State> T create(WFCBaseGenerator generator, WFCTile tile, Cons<T> cons) {
        T out = (T) stateType.get();
        out.create(generator, tile, this);
        cons.get(out);
        out.init();
        generator.states.add(out);
        return out;
    }

    public boolean canCreateAt(WFCBaseGenerator generator, WFCTile tile) {
        return true;
    }

    protected void initState() {
        try {
            Class<?> current = getClass();

            if (current.isAnonymousClass())
                current = current.getSuperclass();

            while (StateType.class.isAssignableFrom(current)) {
                Class<?> type = Structs.find(current.getDeclaredClasses(), t -> State.class.isAssignableFrom(t) && !t.isInterface());
                if(type != null) {
                    Constructor<? extends State> cons = (Constructor<? extends State>) type.getDeclaredConstructor(type.getDeclaringClass());
                    stateType = () -> {
                        try {
                            return cons.newInstance(this);
                        } catch(Exception e) {
                            throw new RuntimeException(e);
                        }
                    };
                    break;
                }

                current = current.getSuperclass();
            }

        } catch(Throwable e) {
            throw new RuntimeException(e);
        }

        if (stateType == null) {
            throw new RuntimeException("State type was not founded in " + this + " of type " + getClass().getCanonicalName() + ".");
        }
    }

    public <T extends StateType> T as() {
        return (T) this;
    }

    public abstract class State implements WFCTile.TileState {
        protected int step = -1;
        public WFCTile tile;
        public StateType type;
        protected WFCBaseGenerator generator;

        public void create(WFCBaseGenerator generator, WFCTile tile, StateType type) {
            this.generator = generator;
            this.tile = tile;
            this.type = type;
        }

        public void init() {

        }

        public void step() {
            step++;
        }

        public boolean isActive() {
            return false;
        }

        public boolean forDelete() {
            return false;
        }

        public <T extends State> T as() {
            return (T) this;
        }
    }
}
