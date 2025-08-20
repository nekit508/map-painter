package com.github.nekit508.mappainter.gui.n;

import arc.func.Prov;
import arc.struct.Seq;
import com.github.nekit508.plcf.lang.compiletime.lexer.Lexer;
import com.github.nekit508.plcf.lang.compiletime.lexer.LexerRule;
import com.github.nekit508.plcf.lang.utils.PosProvider;
import com.github.nekit508.plcf.lang.utils.ReusableStream;

public class GUILexer extends Lexer<GUIContext> {
    public char[] whitespaces = {' ', '\t', '\r'};
    public char[] newLines = {'\n'};

    public char[] commentMarkers = {'/'};
    public char[] multilineCommentMarkers = {'/'};

    public char[] stringQuotes = {'\'', '"'};

    public char[] numberStart = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '_'
    };
    public char[] numberBody = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    };
    public char[] numberEnd = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'i', 'l', 's', 'b', 'f', 'd',
            'I', 'L', 'S', 'B', 'F', 'D'
    };

    public char[] identStart = {'_', '$'};
    public char[] identBody = {};

    public String[] keywords = {
            "default",
            "func",
            "create",
            "true",
            "false"
    };
    public GUITokenKinds[] keywordKinds = {
            GUITokenKinds.DEFAULT,
            GUITokenKinds.FUNC,
            GUITokenKinds.CREATE,
            GUITokenKinds.BOOLEAN,
            GUITokenKinds.BOOLEAN
    };

    public LexerRule<GUILexer> skipWhiteSpacesRule;
    public LexerRule<GUILexer> skipCommentsRule;
    public LexerRule<GUILexer> skipSymbols;
    public LexerRule<GUILexer> stringRule;
    public LexerRule<GUILexer> singleSymbolTokenRule;
    public LexerRule<GUILexer> numberRule;
    public LexerRule<GUILexer> identOrKeywordRule;

    public Seq<LexerRule<GUILexer>> rootRules = new Seq<>();

    public GUILexer(ReusableStream<Character> compileSource, PosProvider posProvider, Seq<Prov<Seq<Character>>> identSymbolsProviders, GUIContext context) {
        super(compileSource, posProvider, context);

        Seq<Character> symbols = new Seq<>();
        for (Prov<Seq<Character>> printableSymbolsProvider : identSymbolsProviders)
            symbols.addAll(printableSymbolsProvider.get());

        var chars = new char[symbols.size];
        for (int i = 0; i < symbols.size; i++)
                chars[i] = symbols.get(i);

        var newIdentStart = new char[identStart.length + chars.length];
        System.arraycopy(identStart, 0, newIdentStart, 0, identStart.length);
        System.arraycopy(chars, 0, newIdentStart, identStart.length, chars.length);
        identStart = newIdentStart;

        var newIdentBody = new char[identBody.length + chars.length];
        System.arraycopy(identBody, 0, newIdentStart, 0, identBody.length);
        System.arraycopy(chars, 0, newIdentStart, identBody.length, chars.length);
        identBody = newIdentBody;

        skipWhiteSpacesRule = com.github.nekit508.plcf.lang.compiletime.lexer.LexerRule.createSkip("skipWhiteSpaces", whitespaces);
        skipCommentsRule = LexerRule.createSkipComments("skipComments", newLines, commentMarkers, multilineCommentMarkers);
        skipSymbols = LexerRule.parseWhileParsing("skipMiscSymbols", skipCommentsRule, skipWhiteSpacesRule);

        stringRule = LexerRule.createStringParser("string", GUITokenKinds.STRING, stringQuotes);
        numberRule = LexerRule.createNumberParser("number",GUITokenKinds.NUMBER , numberStart, numberBody, numberEnd);
        identOrKeywordRule = LexerRule.createIdentOrKeywordParser("identOrKeyword", GUITokenKinds.IDENT, identStart, identBody, keywords, keywordKinds);

        singleSymbolTokenRule = LexerRule.createOneSymbolParser("singleSymbolToken", GUITokenKinds.SingleSymbol.values());

        rootRules.addAll(skipSymbols, singleSymbolTokenRule, identOrKeywordRule, numberRule, stringRule);
    }

    @Override
    public Seq<LexerRule<GUILexer>> getRootRules() {
        return null;
    }
}
