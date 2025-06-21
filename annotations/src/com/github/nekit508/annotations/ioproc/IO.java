package com.github.nekit508.annotations.ioproc;

import arc.util.io.Reads;
import arc.util.io.Writes;

/** Interface used for code insight, will be deleted from AST before bytecode translation. */
public interface IO {
    default void read(Reads reads) {}
    default void write(Writes writes) {}

    /** This method used in generated code, do not call manually. */
    default void beforeRead(Reads reads) {}
    /** This method used in generated code, do not call manually. */
    default void afterRead(Reads reads) {}

    /** This method used in generated code, do not call manually. */
    default void beforeWrite(Writes writes) {}
    /** This method used in generated code, do not call manually. */
    default void afterWrite(Writes writes) {}
}
