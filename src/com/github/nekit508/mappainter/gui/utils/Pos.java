package com.github.nekit508.mappainter.gui.utils;

public class Pos {
    public int line;
    public int pos;

    public Pos(int line, int pos) {
        this.line = line;
        this.pos = pos;
    }

    @Override
    public String toString() {
        return "(" + (line+1) + ":" + (pos) + ')';
    }

    public Pos copy() {
        return new Pos(line, pos);
    }

    public void set(int line, int pos) {
        this.line = line;
        this.pos = pos;
    }
}
