package com.github.nekit508.mappainter;

import arc.math.geom.Vec2;
import arc.util.io.Reads;
import arc.util.io.Writes;
import com.github.nekit508.mindustry.annotations.ioproc.IOProc;

@IOProc.IOProvider
public class TypeIO {
    public Vec2 readVec2(Reads reads) {
        var out = new Vec2();
        out.x = reads.f();
        out.y = reads.f();
        return out;
    }

    public void writeVec2(Vec2 vec, Writes writes) {

    }
}
