package com.github.nekit508.mappainter.gui.compiletime;

import arc.struct.Seq;
import com.github.bsideup.jabel.Desugar;
import com.github.nekit508.mappainter.gui.Context;
import com.github.nekit508.mappainter.gui.Tree;
import com.github.nekit508.mappainter.gui.exceptions.ParserException;
import com.github.nekit508.mappainter.gui.utils.Logger;
import com.github.nekit508.mappainter.gui.utils.ReusableStream;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public abstract class Parser<T extends Context> {
    public ReusableStream<Token> tokenStream;
    public T context;
    public Logger log = new Logger(false);

    public Parser(ReusableStream<Token> stream, T context) {
        this.tokenStream = stream;
        this.context = context;
    }

    public Token current() {
        return tokenStream.get();
    }
    
    public void redo() {
        tokenStream.redo();
    }

    public void redo(int num) {
        tokenStream.redo(num);
    }

    public boolean probe(Token.Kind... kind) throws ParseFail {
        var token = next();
        redo();

        for (int i = 0; i < kind.length; i++)
            if (kind[i] == token.kind)
                return true;

        return false;
    }

    public boolean hasAnyToken() throws ParseFail {
        var token = next();
        redo();

        return token.kind != Token.Kind.EOT;
    }

    public Token next() throws ParseFail {
        var token = tokenStream.next();

        if (token == null)
            throw new ParseFail("Token is null.");

        return token;
    }

    public Token accept(Token.Kind... requiredTokenKinds) throws ParseFail {
        var token = next();

        for (int i = 0; i < requiredTokenKinds.length; i++)
            if (requiredTokenKinds[i] == token.kind)
                return token;

        throw new ParseFail("Required any of " + new Seq<>(requiredTokenKinds) + ", but " + token.kind + " provided. At " + token.pos);
    }

    public Token except(Token.Kind... exceptedTokenKinds) throws ParseFail {
        var token = next();

        for (int i = 0; i < exceptedTokenKinds.length; i++)
            if (exceptedTokenKinds[i] == token.kind)
                throw new ParseFail("Excepted all of " + new Seq<>(exceptedTokenKinds) + ", but " + token.kind + " provided. At " + token.pos);

        return token;
    }

    @SafeVarargs
    public final <R extends Tree> R parseAny(ParserFeature<T, ? extends R>... features) throws ParseFail {
        String causes = "";

        for (var feature : features) {
            try {
                tokenStream.saveState();
                var out = parse(feature);
                tokenStream.disposeState();
                return out;
            } catch (ParseFail e) {
                tokenStream.redoState();
                var bytes = new ByteArrayOutputStream();
                e.printStackTrace(new PrintStream(bytes));
                causes += "\n" + bytes;
            }
        }

        var pos = tokenStream.next().pos;
        redo();
        String out = "";
        for (ParserFeature<T, ? extends R> feature : features)
            out += " " + feature.name;
        throw new ParseFail("Failed to parse any of" + out + " at " + pos + "." + causes);
    }

    public <R extends Tree> R parse(ParserFeature<T, R> feature) throws ParseFail {
        var pos = next().pos;
        redo();
        try {
            log.info("Parsing feature @ at @", feature.name, pos);
            return feature.parse(this);
        } catch (ParseFail e) {
            log.info("Failed feature @ at @", feature.name, pos);
            throw new ParseFail("Failed to parse " + feature.name + " at " + pos + ".", e);
        }
    }

    public abstract Tree.Unit parseCompileSource() throws ParserException ;
    /*{
        try {
            return unitParserFeature.parse(new ParserContext<>((Parser<InterpreterContext>) this));
        } catch (ParseFail e) {
            throw new RuntimeException(e);
        }
        log.info("Parse compilation unit.");
        Seq<Tree.FuncDecl> members = new Seq<>();

        while (hasAnyToken()) {
            Tree.FuncDecl member = parseCompileSourceMember();
            members.add(member);
        }

        return new Tree.Unit(Tree.Kind.UNIT, members);
    }

    /*public Tree.FuncDecl parseCompileSourceMember() {
        log.info("Parse unit member.");
        return parseAny(
                this::parseFunctionDeclaration
        );
    }

    public Tree.Ident parseIdent() {
        log.info("Parsing ident.");
        return new Tree.Ident(Tree.Kind.IDENT, accept(Token.Kind.IDENT).literal);
    }

    public Tree.StringValue parseString() {
        log.info("Parsing string.");
        return new Tree.StringValue(Tree.Kind.STRING_VALUE, accept(Token.Kind.STRING).literal);
    }

    public Tree.NumericValue parseNumber() {
        log.info("Parsing number.");
        return new Tree.NumericValue(Tree.Kind.NUMERIC_VALUE, parseNumberFromLiteral(accept(Token.Kind.NUMBER).literal));
    }

    public Tree.Value parseAnyValue() {
        log.info("Parsing value.");
        var value = parseAny(this::parseNumber, this::parseIdent, this::parseString, this::parseBoolean);

        if (value == null)
            throw new UnexpectedTokenKindException("Unable to parse VALUE.");

        return value;
    }

    public Tree.Assignment parseAssignment() {
        log.info("Parsing assignment.");

        var key = parseIdent();
        accept(Token.Kind.COLON);
        var value = parseAnyValue();

        return new Tree.Assignment(Tree.Kind.ASSIGNMENT, key, value);
    }

    public Tree parseElementBodyMember() {
        log.info("Parsing element body member");

        var out = parseAny(
                this::parseAssignment,
                this::parseElementDeclaration,
                this::parseMethodExec,
                this::parseDefaultsSettings,
                this::parseFunctionExecution
        );

        if (out == null)
            throw new UnexpectedTokenKindException("Unable to parse ASSIGNMENT/ELEMENT_DECL/METHOD_EXEC/DEFAULTS_SETTINGS.");
        return out;
    }

    public Tree.DefaultsSettings parseDefaultsSettings() {
        var methods = new Seq<Tree.MethodExec>();

        accept(Token.Kind.DEFAULTS);
        accept(Token.Kind.L_BRACE);

        while (!probe(Token.Kind.R_BRACE))
            methods.add(parseMethodExec());

        accept(Token.Kind.R_BRACE);

        return new Tree.DefaultsSettings(Tree.Kind.DEFAULTS_SETTINGS, methods);
    }

    public Tree.ElementBody parseElementBody() {
        log.info("Parsing element body");

        accept(Token.Kind.L_BRACE);

        var members = new Seq<Tree>();

        while (!probe(Token.Kind.R_BRACE)) {
            var tree = parseElementBodyMember();

            members.add(tree);
        }

        accept(Token.Kind.R_BRACE);

        return new Tree.ElementBody(Tree.Kind.ELEMENT_BODY, members);
    }

    public Tree.MethodExec parseMethodExec() {
        var method = parseIdent();
        var values = new Seq<Tree.Value>();

        accept(Token.Kind.L_PAREN);

        while (!probe(Token.Kind.R_PAREN)) {
            values.add(parseAnyValue());
        }

        accept(Token.Kind.R_PAREN);

        return new Tree.MethodExec(Tree.Kind.METHOD_EXEC, method, values);
    }

    public Tree.ElementDecl parseElementDeclaration() {
        log.info("Parsing element declaration");
        var method = parseIdent();

        var args = new Seq<Tree.Value>();
        if (probe(Token.Kind.L_PAREN)) {
            tokenStream.next();
            args.add(parseAnyValue());
            accept(Token.Kind.R_PAREN);
        }

        var body = parseElementBody();

        var cellMethodExecutions = new Seq<Tree.MethodExec>();
        if (probe(Token.Kind.L_BRACKET)) {
            accept(Token.Kind.L_BRACKET);

            while (!probe(Token.Kind.R_BRACKET))
                cellMethodExecutions.add(parseMethodExec());

            accept(Token.Kind.R_BRACKET);
        }

        return new Tree.ElementDecl(Tree.Kind.ELEMENT_DECL, method, body, args, cellMethodExecutions);
    }

    public Tree.BooleanValue parseBoolean() {
        boolean value = probe(Token.Kind.TRUE);

        accept(Token.Kind.TRUE, Token.Kind.FALSE);

        return new Tree.BooleanValue(Tree.Kind.BOOLEAN_VALUE, value);
    }

    public Tree.FuncDecl parseFunctionDeclaration() {
        accept(Token.Kind.FUNC);

        var ident = parseIdent();
        var body = parseElementBody();

        return new Tree.FuncDecl(Tree.Kind.FUNC_DECL, ident, body);
    }

    public Tree.FuncExec parseFunctionExecution() {
        accept(Token.Kind.DOT);

        var ident = parseIdent();

        return new Tree.FuncExec(Tree.Kind.FUNC_EXEC, ident);
    }*/

    @Desugar
    public record ParserFeature<P extends Context, O extends Tree>(String name, ParserFeatureParser<P, O> parser) {
        public O parse(Parser<P> context) throws ParseFail {
            return parser().parse(context);
        }
    }

    @FunctionalInterface
    public interface ParserFeatureParser<P extends Context, O extends Tree> {
        O internalParse(Parser<P> cont) throws ParseFail;

        default O parse(Parser<P> cont) throws ParseFail {
            return internalParse(cont);
        }
    }

    public static class ParseFail extends Throwable {
        public ParseFail() {
            super();
        }

        public ParseFail(String message) {
            super(message);
        }

        public ParseFail(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
