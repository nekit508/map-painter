package com.github.nekit508.mappainter.map.generator;

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

    public <T extends State> T create(WFCBaseGenerator generator, int x, int y) {
        var out = stateType.get();
        out.set(generator, x, y, this);
        out.init();
        process(out);
        return (T) out;
    }

    public <T extends State> void process(T state) {}

    public abstract boolean canCreateAt(WFCBaseGenerator generator, int x, int y);

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

    public class State {
        protected int step = -1;
        public int x, y;
        public StateType type;
        protected WFCBaseGenerator generator;

        public void set(WFCBaseGenerator generator, int x, int y, StateType type) {
            this.y = y;
            this.x = x;
            this.type = type;
            this.generator = generator;
        }

        public void init() {
            generator.activeStates.add(this);
        }

        public void fill() {

        }

        public void step() {
            step++;
        }

        public boolean active() {
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
