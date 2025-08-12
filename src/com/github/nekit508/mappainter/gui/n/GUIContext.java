package com.github.nekit508.mappainter.gui.n;

import arc.struct.Seq;
import com.github.nekit508.plcf.lang.Context;
import com.github.nekit508.plcf.lang.ContextRules;
import com.github.nekit508.plcf.lang.Tree;
import com.github.nekit508.plcf.lang.TreeWalker;
import com.github.nekit508.plcf.lang.compiletime.CompileSource;
import com.github.nekit508.plcf.lang.compiletime.CompileTask;
import com.github.nekit508.plcf.lang.compiletime.Compiler;
import com.github.nekit508.plcf.lang.compiletime.lexer.Lexer;
import com.github.nekit508.plcf.lang.compiletime.parser.Parser;
import com.github.nekit508.plcf.lang.compiletime.token.Token;
import com.github.nekit508.plcf.lang.utils.PosProvider;
import com.github.nekit508.plcf.lang.utils.PosProviderCharReusableStreamWrapper;
import com.github.nekit508.plcf.lang.utils.ReusableStream;

public class GUIContext extends Context<ContextRules> {
    public GUIContext(ContextRules rules) {
        super(rules);
    }

    @Override
    public <T extends CompileTask<?>, O extends CompileSource> T getCompileTaskFor(O input) {
        return null;
    }

    @Override
    public <T extends Compiler<?>> T getCompiler(Tree root) {
        return null;
    }

    @Override
    public <T extends Parser<?>> T getParser(ReusableStream<Token> stream) {
        return null;
    }

    @Override
    public <T extends Lexer<?>> T getLexer(ReusableStream<Character> stream) {
        var s = stream instanceof PosProvider ? stream : new PosProviderCharReusableStreamWrapper(stream);
        return (T) new GUILexer(s, (PosProvider) s, Seq.with(() -> {
            var out = new Seq<Character>();

            for (char i = 65; i < 91; i++)
                out.add(i);
            for (char i = 97; i < 123; i++)
                out.add(i);

            return out;
        }), this);
    }

    @Override
    public <T extends TreeWalker<?>> Seq<T> getAnalyzers() {
        return null;
    }
}
