package com.github.nekit508.mappainter.gui.n;

import com.github.nekit508.plcf.lang.compiletime.token.TokenKind;

public enum TokenKinds implements TokenKind {
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
    GRATE
}