package com.github.nekit508.mappainter.gui.utils;

import java.io.IOException;
import java.io.InputStreamReader;

public class ReusableCharStream extends ReusableStreamAbstractImpl<Character> {
    public InputStreamReader reader;
    public char[] buf;

    public ReusableCharStream(InputStreamReader reader, int bufferSize) {
        this.reader = reader;
        buf = new char[bufferSize];
    }

    @Override
    public Character readNextObject() {
        try {
            var c = (char) reader.read();
            if (c == (char)-1)
                throw new EOSException();
            return c;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
