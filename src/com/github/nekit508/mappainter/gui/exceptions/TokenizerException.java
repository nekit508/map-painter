package com.github.nekit508.mappainter.gui.exceptions;

import com.github.nekit508.mappainter.gui.utils.Pos;

public class TokenizerException extends Throwable {
    public Pos pos;

    public TokenizerException(String message, Pos pos) {
        super(message);
        this.pos = pos;
    }

    public TokenizerException(Throwable cause, Pos pos) {
        super(cause);
        this.pos = pos;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + " At " + pos + ".";
    }

    public static class UnhandledTokenizerException extends RuntimeException {
        public UnhandledTokenizerException(TokenizerException cause) {
            super(cause);
        }
    }
}
