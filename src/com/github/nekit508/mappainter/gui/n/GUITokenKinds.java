package com.github.nekit508.mappainter.gui.n;

import arc.func.Prov;
import com.github.nekit508.plcf.lang.compiletime.token.TokenKind;

public enum GUITokenKinds implements TokenKind {
    STRING,
    NUMBER,
    IDENT,
    BOOLEAN,

    FUNC,
    CREATE,
    DEFAULT;

    enum SingleSymbol implements TokenKind, Prov<Character> {
        L_BRACKET('['),
        R_BRACKET(']'),

        L_BRACE('{'),
        R_BRACE('}'),

        L_PAREN('('),
        R_PAREN(')'),

        DOT('.'),
        GRATE('#');

        public final char symbol;

        SingleSymbol(char symbol) {
            this.symbol = symbol;
        }

        @Override
        public Character get() {
            return symbol;
        }
    }
}
