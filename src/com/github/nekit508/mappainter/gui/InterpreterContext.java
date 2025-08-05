package com.github.nekit508.mappainter.gui;

import arc.struct.Seq;
import com.github.nekit508.mappainter.gui.compiletime.Compiler;
import com.github.nekit508.mappainter.gui.compiletime.*;
import com.github.nekit508.mappainter.gui.runtime.Interpreter;
import com.github.nekit508.mappainter.gui.utils.ReusableStream;

@SuppressWarnings("unchecked")
public class InterpreterContext extends Context {
    public Interpreter interpreter = new Interpreter(this);

    @Override
    public <T extends CompileTask<?>, O extends CompileSource> T getCompileTaskFor(O object) {
        return (T) new InterpreterCompileTask(object, this);
    }

    @Override
    public <T extends Compiler<?>> T getCompiler() {
        return (T) new InterpreterCompiler(this);
    }

    @Override
    public <T extends Parser<?>> T getParser(ReusableStream<Token> stream) {
        return (T) new InterpreterParser(stream, this);
    }

    @Override
    public <T extends Tokenizer<?>> T getTokenizer(ReusableStream<Character> stream) {
        return (T) new InterpreterTokenizer(stream, this);
    }

    @Override
    public <T extends TreeAnalyzer<?>> Seq<T> getAnalyzers() {
        return Seq.with();
    }
}
