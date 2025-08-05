package com.github.nekit508.mappainter.gui.compiletime;

import arc.struct.IntSeq;
import com.github.nekit508.mappainter.gui.Context;
import com.github.nekit508.mappainter.gui.exceptions.TokenizerException;
import com.github.nekit508.mappainter.gui.utils.Pos;
import com.github.nekit508.mappainter.gui.utils.ReusableStream;
import com.github.nekit508.mappainter.gui.utils.ReusableStreamAbstractImpl;

public class Tokenizer<T extends Context> extends ReusableStreamAbstractImpl<Token> {
    public ReusableStream<Character> reader;
    public T context;
    public StringBuilder cl = new StringBuilder();
    public Token ct;

    public Pos currentPos = new Pos(0, 0);

    public IntSeq linesSizes = new IntSeq();

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

    public Tokenizer(ReusableStream<Character> compileSource, T context) {
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
        var c = getChar();
        if (!in(c, notVisible))
            currentPos.pos -= 1;

        if(c == '\n') {
            currentPos.pos = linesSizes.pop();
            currentPos.line -= 1;
        }

        reader.redo();
    }

    public char getNextChar() {
        var c = reader.next();

        if (!in(c, notVisible))
            currentPos.pos += 1;

        if(c == '\n') {
            linesSizes.add(currentPos.pos);
            currentPos.line += 1;
            currentPos.pos = 0;
        }
        return c;
    }

    public Token parseNextToken() throws TokenizerException {
        try {
            ct = new Token();
            cl.setLength(0);

            kind:
            {
                var c = getNextNotWhitespace();

                ct.pos = currentPos.copy();

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
                if (in(c, identStart)) {
                    parseIdentOrKeyword();
                } else if (in(c, stringStart)) {
                    parseString(c == '"');
                } else if (in(c, numberStart)) {
                    parseNumber();
                }
            }

            if (ct.kind == null)
                throw new TokenizerException("Unknown token kind.", currentPos);
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
        while (in(getNextChar(), whitespace));
        return getChar();
    }

    public void parseIdentOrKeyword() {
        do putChar(getChar());
        while (in(getNextChar(), identBody));
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
        while (in(getNextChar(), numberBody));

        if (in(getChar(), numberEnd))
            putChar(getChar());
        else
            ct.kind = null;
    }

    public boolean in(char c, char[] group) {
        for (int i = 0; i < group.length; i++)
            if (c == group[i])
                return true;

        return false;
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
