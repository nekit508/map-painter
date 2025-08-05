package com.github.nekit508.mappainter.gui.compiletime;

import com.github.nekit508.mappainter.gui.InterpreterContext;
import com.github.nekit508.mappainter.gui.utils.ReusableStream;

public class InterpreterTokenizer extends Tokenizer<InterpreterContext> {
    public InterpreterTokenizer(ReusableStream<Character> compileSource, InterpreterContext context) {
        super(compileSource, context);
    }
}
