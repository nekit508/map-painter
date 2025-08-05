package com.github.nekit508.mappainter.gui.compiletime;

import com.github.nekit508.mappainter.gui.Context;
import com.github.nekit508.mappainter.gui.exceptions.TaskException;

public abstract class CompileTask<T extends Context> {
    public CompileSource compileSource;
    public T context;

    public CompileTask(CompileSource compileSource, T context) {
        this.compileSource = compileSource;
        this.context = context;
    }

    public abstract void run() throws TaskException;
}
