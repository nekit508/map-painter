package com.github.nekit508.mappainter.utils;

import arc.func.Func2;

public enum ClipType {
    clamp((c, s) -> c >= s ? s - 1 : c < 0 ? 0 : c),
    cycle((c, s) -> (s + (c % s)) % s);

    public final Func2<Integer, Integer, Integer> func;

    ClipType(Func2<Integer, Integer, Integer> func) {
        this.func = func;
    }

    public int get(int pos, int size) {
        return func.get(pos, size);
    }
}
