package com.github.nekit508.mappainter.gui.compiletime;

import arc.util.Strings;
import com.github.nekit508.mappainter.gui.utils.Pos;

public class Token {
    public String literal;
    public Pos pos;
    public Kind kind;

    @Override
    public String toString() {
        return Strings.format("{[@]@ at @}", kind, literal, pos);
    }

    public enum Kind {
        /** [ */
        L_BRACKET,
        /** ] */
        R_BRACKET,
        /** ( */
        L_PAREN,
        /** ) */
        R_PAREN,
        /** { */
        L_BRACE,
        /** } */
        R_BRACE,
        /** : */
        COLON,
        /** . */
        DOT,
        IDENT,
        NUMBER,
        STRING,
        /** true */
        TRUE,
        /** false */
        FALSE,
        /** null */
        NULL,
        /** defaults */
        DEFAULTS,
        /** func */
        FUNC,
        /** # */
        GRATE,
        EOT
    }
}
