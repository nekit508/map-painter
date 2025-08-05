package com.github.nekit508.mappainter.gui.runtime;

import com.github.nekit508.mappainter.gui.Context;

public abstract class Executable<T extends Context> {
    protected T context;
    public String name;

    public Executable(T context, String name) {
        this.name = name;
        this.context = context;
    }

    public abstract void execute(Object object);
}
