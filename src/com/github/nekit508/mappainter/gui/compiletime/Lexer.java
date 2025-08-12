package com.github.nekit508.mappainter.gui.compiletime;

import com.github.nekit508.mappainter.gui.Context;
import com.github.nekit508.mappainter.gui.exceptions.TokenizerException;
import com.github.nekit508.mappainter.gui.utils.PosProvider;
import com.github.nekit508.mappainter.gui.utils.ReusableStream;
import com.github.nekit508.mappainter.gui.utils.ReusableStreamAbstractImpl;
import com.github.nekit508.mappainter.gui.utils.Utils;

public class Lexer<T extends Context> extends ReusableStreamAbstractImpl<Token> {
    public ReusableStream<Character> reader;
    private final PosProvider positionProvider;
    public T context;
    public StringBuilder cl = new StringBuilder();
    public Token ct;

    public char[] whitespace = {' ', '\n', '\r', '\t'};

    public char[] notVisible = {'\n', '\r'};

    public char[] identStart = {
            'A', 'B', 'C', 'D', 'E',
            'F', 'G', 'H', 'I', 'J',
            'K', 'L', 'M', 'N', 'O',
            'P', 'Q', 'R', 'S', 'T',
            'U', 'V', 'W', 'X', 'Y',
            'Z',
            'a', 'b', 'c', 'd', 'e',
            'f', 'g', 'h', 'i', 'j',
            'k', 'l', 'm', 'n', 'o',
            'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y',
            'z',
            '$', '_'
    };
    public char[] identBody = {
            'A', 'B', 'C', 'D', 'E',
            'F', 'G', 'H', 'I', 'J',
            'K', 'L', 'M', 'N', 'O',
            'P', 'Q', 'R', 'S', 'T',
            'U', 'V', 'W', 'X', 'Y',
            'Z',
            'a', 'b', 'c', 'd', 'e',
            'f', 'g', 'h', 'i', 'j',
            'k', 'l', 'm', 'n', 'o',
            'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y',
            'z',
            '$', '_',
            '0', '1', '2', '3', '4',
            '5', '6', '7', '8', '9'
    };

    public char[] numberStart = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    };

    public char[] numberEnd = {
            'b', 'i', 'l', 's', 'f', 'd',
            'B', 'I', 'L', 'S', 'F', 'D'
    };

    public char[] numberBody = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    };

    public char[] stringStart = {'\'', '"'};

    public String[] keywords = {
            "defaults",
            "func",
            "true",
            "false",
            "null"
    };

    public Token.Kind[] keywordsKinds = {
            Token.Kind.DEFAULTS,
            Token.Kind.FUNC,
            Token.Kind.TRUE,
            Token.Kind.FALSE,
            Token.Kind.NULL
    };

    public Lexer(ReusableStream<Character> compileSource, PosProvider positionProvider, T context) {
        this.positionProvider = positionProvider;
        this.context = context;
        reader = compileSource;
    }

    @Override
    public Token readNextObject() {
        try {
            return parseNextToken();
        } catch (TokenizerException e) {
            throw new TokenizerException.UnhandledTokenizerException(e);
        }
    }

    public char getChar() {
        return reader.get();
    }

    public void putChar(char c) {
        cl.append(c);
    }

    public void redoChar() {
        reader.redo();
    }

    public char getNextChar() {
        return reader.next();
    }

    public Token parseNextToken() throws TokenizerException {
        try {
            ct = new Token();
            cl.setLength(0);

            kind:
            {
                var c = getNextNotWhitespace();

                ct.pos = positionProvider.getCurrentPos();

                // try to parse any single character token
                ct.kind = switch (c) {
                    case '{' -> Token.Kind.L_BRACE;
                    case '}' -> Token.Kind.R_BRACE;

                    case '[' -> Token.Kind.L_BRACKET;
                    case ']' -> Token.Kind.R_BRACKET;

                    case '(' -> Token.Kind.L_PAREN;
                    case ')' -> Token.Kind.R_PAREN;

                    case ':' -> Token.Kind.COLON;

                    case '.' -> Token.Kind.DOT;

                    case '#' -> Token.Kind.GRATE;

                    default -> null;
                };

                if (ct.kind != null)
                    break kind;

                // now try to parse any poly character token
                if (Utils.in(c, identStart)) {
                    parseIdentOrKeyword();
                } else if (Utils.in(c, stringStart)) {
                    parseString(c == '"');
                } else if (Utils.in(c, numberStart)) {
                    parseNumber();
                }
            }

            if (ct.kind == null)
                throw new TokenizerException("Unknown token kind.", ct.pos);
            else {
                ct.literal = cl.toString();
                return ct;
            }
        } catch (EOSException e) {
            ct.kind = Token.Kind.EOT;
            ct.literal = "\000";
            return ct;
        }
    }

    public char getNextNotWhitespace() {
        while (Utils.in(getNextChar(), whitespace));
        return getChar();
    }

    public void parseIdentOrKeyword() {
        do putChar(getChar());
        while (Utils.in(getNextChar(), identBody));
        redoChar();

        ct.kind = Token.Kind.IDENT;

        String str = cl.toString();
        for (int i = 0; i < keywords.length; i++)
            if (keywords[i].equals(str))
                ct.kind = keywordsKinds[i];
    }

    public void parseString(boolean doubleQuotes) {
        while (getNextChar() != (doubleQuotes ? '"' : '\''))
            putChar(getChar());

        ct.kind = Token.Kind.STRING;
    }

    public void parseNumber() {
        ct.kind = Token.Kind.NUMBER;

        do putChar(getChar());
        while (Utils.in(getNextChar(), numberBody));

        if (Utils.in(getChar(), numberEnd))
            putChar(getChar());
        else
            ct.kind = null;
    }

    @Override
    public void dispose() {
        super.dispose();

        if (reader != null) {
            reader.dispose();
            reader = null;
        }

        cl.setLength(0);
        ct = null;
    }
}
