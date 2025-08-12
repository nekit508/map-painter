package com.github.nekit508.mappainter.gui.compiletime;

import com.github.nekit508.mappainter.gui.InterpreterContext;
import com.github.nekit508.mappainter.gui.utils.PosProvider;
import com.github.nekit508.mappainter.gui.utils.ReusableStream;

public class InterpreterLexer extends Lexer<InterpreterContext> {
    public InterpreterLexer(ReusableStream<Character> compileSource, PosProvider posProvider, InterpreterContext context) {
        super(compileSource, posProvider, context);
    }
}
