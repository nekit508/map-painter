package com.github.nekit508.mappainter.gui;

import arc.struct.Seq;
import com.github.nekit508.mappainter.gui.compiletime.*;
import com.github.nekit508.mappainter.gui.runtime.Interpreter;
import com.github.nekit508.mappainter.gui.utils.PosProvider;
import com.github.nekit508.mappainter.gui.utils.PosProviderCharReusableStreamWrapper;
import com.github.nekit508.mappainter.gui.utils.ReusableStream;

@SuppressWarnings("unchecked")
// TODO rewrite compiler and interpreter
public class InterpreterContext extends Context {
    public Interpreter interpreter = new Interpreter(this);

    @Override
    public <T extends CompileTask<?>, O extends CompileSource> T getCompileTaskFor(O object) {
        return (T) new InterpreterCompileTask(object, this);
    }

    @Override
    public InterpreterCompiler getCompiler() {
        return  new InterpreterCompiler(this);
    }

    @Override
    public <T extends Parser<?>> T getParser(ReusableStream<Token> stream) {
        return (T) new InterpreterParser(stream, this);
    }

    @Override
    public <T extends Lexer<?>> T getLexer(ReusableStream<Character> stream) {
        return (T) new InterpreterLexer(stream, stream instanceof PosProvider prov ? prov : new PosProviderCharReusableStreamWrapper(stream), this);
    }

    @Override
    public <T extends TreeAnalyzer<?>> Seq<T> getAnalyzers() {
        return Seq.with();
    }
}
