package com.github.nekit508.annotations.ioproc;

import arc.util.io.Reads;
import arc.util.io.Writes;

public class DummyMethods {
    public static void write(Writes writes, Object object) {

    }

    public static <T> T read(Reads reads, T object) {
        return null;
    }

    public static <T> T read(Reads reads) {
        return null;
    }
}
