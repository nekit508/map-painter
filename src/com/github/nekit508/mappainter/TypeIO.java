package com.github.nekit508.mappainter;

import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.io.Reads;
import arc.util.io.Writes;
import com.github.nekit508.annotations.ioproc.IOAnnotations;

@IOAnnotations.Provider
public class TypeIO {
    @IOAnnotations.Provider.UniProvider
    public static <T> void write(Writes writes, T object) {
    }

    @IOAnnotations.Provider.UniProvider
    public static <T> T read(Reads reads, T onject) {
        return null;
    }

    @IOAnnotations.Provider.UniProvider
    public static <T> T readCreate(Reads reads) {
        return null;
    }

    public static Vec2 readVec2(Reads reads) {
        var out = new Vec2();
        return readVec2(reads, out);
    }

    public static Vec2 readVec2(Reads reads, Vec2 vec2) {
        vec2.x = reads.f();
        vec2.y = reads.f();
        return vec2;
    }

    public static void writeVec2(Writes writes, Vec2 vec) {
        writes.f(vec.x);
        writes.f(vec.y);
    }

    public static <T> Seq<T> readSeq(Reads reads, Seq<T> seq) {
        seq.clear();
        int size = reads.i();
        for (int i = 0; i < size; i++) {
            T obj = readCreate(reads);
            seq.add(obj);
        }
        return seq;
    }

    public static <T> Seq<T> readSeq(Reads reads) {
        var seq = new Seq<T>();
        seq = readSeq(reads, seq);
        return seq;
    }

    public static <T> void writeSeq(Writes writes, Seq<T> seq) {
        writes.i(seq.size);
        for (T t : seq)
            write(writes, t);
    }
}
