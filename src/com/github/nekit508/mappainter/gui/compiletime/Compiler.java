package com.github.nekit508.mappainter.gui.compiletime;

import arc.struct.Seq;
import com.github.nekit508.mappainter.gui.Context;
import com.github.nekit508.mappainter.gui.Tree;
import com.github.nekit508.mappainter.gui.exceptions.CompileException;
import com.github.nekit508.mappainter.gui.runtime.Executable;

public abstract class Compiler<T extends Context> {
    public T context;

    public Compiler(T context) {
        this.context = context;
    }

    public abstract Seq<Executable<T>> compile(Tree.Unit tree) throws CompileException;
}
