package com.github.nekit508.mappainter.gui.compiletime;

import com.github.nekit508.mappainter.gui.utils.ReusableStream;

/** Class that represents something that contains characters for tokenizing. */
public interface CompileSource {
    ReusableStream<Character> getInputStream();
}
