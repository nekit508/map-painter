package com.github.nekit508.mappainter.gui.compiletime;

import arc.struct.Seq;
import com.github.nekit508.mappainter.gui.InterpreterContext;
import com.github.nekit508.mappainter.gui.Tree;
import com.github.nekit508.mappainter.gui.exceptions.ParserException;
import com.github.nekit508.mappainter.gui.utils.ReusableStream;

public class InterpreterParser extends Parser<InterpreterContext> {
    public static ParserFeature<InterpreterContext, Tree.Unit> unitFeature;
    public static ParserFeature<InterpreterContext, Tree.FuncDecl> funcDeclFeature;
    public static ParserFeature<InterpreterContext, Tree.Ident> identFeature;
    public static ParserFeature<InterpreterContext, Tree.ElementBody> elementBodyFeature;
    public static ParserFeature<InterpreterContext, Tree> elementBodyMemberFeature;

    public static ParserFeature<InterpreterContext, Tree.Assignment> assignmentFeature;
    public static ParserFeature<InterpreterContext, Tree.ElementDecl> elementDeclarationFeature;
    public static ParserFeature<InterpreterContext, Tree.MethodExec> methodExecFeature;
    public static ParserFeature<InterpreterContext, Tree.DefaultsSettings> defaultsSettingsFeature;
    public static ParserFeature<InterpreterContext, Tree.FuncExec> functionExecutionFeature;

    public static ParserFeature<InterpreterContext, Tree.Value> valueFeature;
    public static ParserFeature<InterpreterContext, Tree.StringValue> stringValueFeature;
    public static ParserFeature<InterpreterContext, Tree.NumericValue> numericValueFeature;
    public static ParserFeature<InterpreterContext, Tree.BooleanValue> booleanValueFeature;

    static {
        unitFeature = new ParserFeature<>("unit", c -> {
            var members = new Seq<Tree.FuncDecl>();

            while (c.hasAnyToken())
                members.add(c.parse(funcDeclFeature));

            return new Tree.Unit(Tree.Kind.UNIT, members);
        });

        funcDeclFeature = new ParserFeature<>("funcDecl", c -> {
            c.accept(Token.Kind.FUNC);

            var ident = c.parse(identFeature);
            var body = c.parse(elementBodyFeature);

            return new Tree.FuncDecl(Tree.Kind.FUNC_DECL, ident, body);
        });

        identFeature = new ParserFeature<>("ident", c ->
                new Tree.Ident(Tree.Kind.IDENT, c.accept(Token.Kind.IDENT).literal)
        );

        elementBodyFeature = new ParserFeature<>("elementBody", c -> {
            c.accept(Token.Kind.L_BRACE);

            var members = new Seq<Tree>();
            while (!c.probe(Token.Kind.R_BRACE))
                members.add(c.parse(elementBodyMemberFeature));

            c.accept(Token.Kind.R_BRACE);

            return new Tree.ElementBody(Tree.Kind.ELEMENT_BODY, members);
        });

        assignmentFeature = new ParserFeature<>("assignment", c -> {
            var key = c.parse(identFeature);
            c.accept(Token.Kind.COLON);
            var value = c.parse(valueFeature);

            return new Tree.Assignment(Tree.Kind.ASSIGNMENT, key, value);
        });

        elementDeclarationFeature = new ParserFeature<>("elementDecl", c -> {
            var method = c.parse(identFeature);
            Tree.Ident name = null;

            var args = new Seq<Tree.Value>();
            if (c.probe(Token.Kind.L_PAREN)) {
                c.next();
                args.add(c.parse(valueFeature));
                c.accept(Token.Kind.R_PAREN);
            }

            var body = c.parse(elementBodyFeature);

            var cellMethodExecutions = new Seq<Tree.MethodExec>();
            if (c.probe(Token.Kind.L_BRACKET)) {
                c.next();

                while (!c.probe(Token.Kind.R_BRACKET)) {
                    cellMethodExecutions.add(c.parse(methodExecFeature));
                }

                c.accept(Token.Kind.R_BRACKET);
            }

            return new Tree.ElementDecl(Tree.Kind.ELEMENT_DECL, method, body, args, cellMethodExecutions, name);
        });

        valueFeature = new ParserFeature<>("value", c -> {
            var value = c.parseAny(
                    numericValueFeature,
                    identFeature,
                    stringValueFeature,
                    booleanValueFeature
            );

            if (value == null)
                throw new ParseFail();

            return value;
        });

        stringValueFeature = new ParserFeature<>("stringValue", c ->
                new Tree.StringValue(Tree.Kind.STRING_VALUE, c.accept(Token.Kind.STRING).literal)
        );

        numericValueFeature = new ParserFeature<>("numericValue", c -> {
            var literal = c.accept(Token.Kind.NUMBER).literal;
            try {
                Object out = null;

                String numberBody = literal.substring(0, literal.length() - 1);
                char numberType = literal.charAt(literal.length() - 1);

                if (numberType == 'i' || numberType == 'I')
                    out = Integer.parseInt(numberBody);
                else if (numberType == 'f' || numberType == 'F')
                    out = Float.parseFloat(numberBody);
                else if (numberType == 's' || numberType == 'S')
                    out = Short.parseShort(numberBody);
                else if (numberType == 'l' || numberType == 'L')
                    out = Long.parseLong(numberBody);
                else if (numberType == 'd' || numberType == 'D')
                    out = Double.parseDouble(numberBody);
                else if (numberType == 'b' || numberType == 'B')
                    out = Byte.parseByte(numberBody);

                if (out == null)
                    throw new ParseFail("Wrong number type " + numberType + ".");

                return new Tree.NumericValue(Tree.Kind.NUMERIC_VALUE, out);
            } catch (NumberFormatException e) {
                throw new ParseFail("Wrong number body in " + literal + ".");
            }
        });

        booleanValueFeature = new ParserFeature<>("booleanValue", c -> {
            boolean value = c.accept(Token.Kind.TRUE, Token.Kind.FALSE).kind == Token.Kind.TRUE;

            return new Tree.BooleanValue(Tree.Kind.BOOLEAN_VALUE, value);
        });

        methodExecFeature = new ParserFeature<>("methodExec", c -> {
            var method = c.parse(identFeature);
            var values = new Seq<Tree.Value>();

            c.accept(Token.Kind.L_PAREN);

            while (!c.probe(Token.Kind.R_PAREN)) {
                values.add(c.parse(valueFeature));
            }

            c.accept(Token.Kind.R_PAREN);

            // like it is element decl, not method exec
            if (c.probe(Token.Kind.L_BRACE))
                throw new ParseFail("Unexpected " + Token.Kind.L_BRACE + ".");

            return new Tree.MethodExec(Tree.Kind.METHOD_EXEC, method, values);
        });

        defaultsSettingsFeature = new ParserFeature<>("defaultsSettings", c -> {
            var methods = new Seq<Tree.MethodExec>();

            c.accept(Token.Kind.DEFAULTS);
            c.accept(Token.Kind.L_BRACE);

            while (!c.probe(Token.Kind.R_BRACE)) {
                methods.add(c.parse(methodExecFeature));
            }

            c.accept(Token.Kind.R_BRACE);

            return new Tree.DefaultsSettings(Tree.Kind.DEFAULTS_SETTINGS, methods);
        });

        functionExecutionFeature = new ParserFeature<>("functionExecution", c -> {
            c.accept(Token.Kind.DOT);

            var ident = c.parse(identFeature);

            return new Tree.FuncExec(Tree.Kind.FUNC_EXEC, ident);
        });

        elementBodyMemberFeature = new ParserFeature<>("elementBodyMember", c -> c.parseAny(
                assignmentFeature,
                elementDeclarationFeature,
                methodExecFeature,
                defaultsSettingsFeature,
                functionExecutionFeature
        ));
    }

    public InterpreterParser(ReusableStream<Token> stream, InterpreterContext context) {
        super(stream, context);
    }

    @Override
    public Tree.Unit parseCompileSource() throws ParserException {
        try {
            return parse(unitFeature);
        } catch (ParseFail e) {
            throw new ParserException(e);
        }
    }
}
