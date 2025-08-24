package com.github.nekit508.mappainter.gui.n;

import arc.struct.Seq;
import com.github.nekit508.plcf.lang.Tree;
import com.github.nekit508.plcf.lang.compiletime.parser.Parser;
import com.github.nekit508.plcf.lang.compiletime.parser.ParserRule;
import com.github.nekit508.plcf.lang.compiletime.token.DefaultTokenKinds;
import com.github.nekit508.plcf.lang.compiletime.token.Token;
import com.github.nekit508.plcf.lang.utils.ReusableStream;

public class GUIParser extends Parser<GUIContext> {
    public ParserRule<GUIParser, GUITree.File> file;
    public ParserRule<GUIParser, GUITree.Function> function;

    public GUIParser(ReusableStream<Token> stream, GUIContext context) {
        super(stream, context);

        file = new ParserRule<>("file", p -> {
            var out = new Seq<GUITree.Function>();

            while (probeNotAndRedoIf(true, DefaultTokenKinds.EOT))
                out.add(parse(function));

            return new GUITree.File(GUITreeKinds.FILE, out);
        });

        function = new ParserRule<>("function", p -> {
            p.accept(GUITokenKinds.FUNC);
            String name = p.accept(GUITokenKinds.IDENT).literal;
            var params = new Seq<String>();
            var closures = new Seq<GUITree.Closure>();

            

            return new GUITree.Function(GUITreeKinds.FUNCTION, name, params, closures);
        });
    }

    @Override
    public <O extends Tree, P extends Parser<GUIContext>> ParserRule<P, O> getRootRule() {
        return (ParserRule<P, O>) file;
    }
}
