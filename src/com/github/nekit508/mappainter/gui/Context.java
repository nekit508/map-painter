package com.github.nekit508.mappainter.gui;

import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Disposable;
import com.github.nekit508.mappainter.gui.compiletime.Compiler;
import com.github.nekit508.mappainter.gui.compiletime.*;
import com.github.nekit508.mappainter.gui.runtime.Executable;
import com.github.nekit508.mappainter.gui.utils.ReusableStream;

/** Class that contains static symbols. */
public abstract class Context implements Disposable {
    public ObjectMap<String, Executable<?>> executables = new ObjectMap<>();

    public <T extends Executable<?>> T getExecutableByName(String name) {
        return (T) executables.get(name);
    }

    public <T extends Executable<?>> void addExecutable(T executable) {
        executables.put(executable.name, executable);
    }

    public abstract <T extends CompileTask<?>, O extends CompileSource> T getCompileTaskFor(O object);

    public abstract <T extends Compiler<?>> T getCompiler();
    public abstract <T extends Parser<?>> T getParser(ReusableStream<Token> stream);
    public abstract <T extends Lexer<?>> T getLexer(ReusableStream<Character> stream);
    public abstract <T extends TreeAnalyzer<?>> Seq<T> getAnalyzers();

    @Override
    public void dispose() {

    }
}
