package com.github.nekit508.mappainter.io;

import arc.util.io.Reads;
import arc.util.io.Writes;
import com.github.nekit508.mappainter.core.MPCore;
import mindustry.io.SaveFileReader;

import java.io.DataInput;
import java.io.DataOutput;

public class MPCustomChunk implements SaveFileReader.CustomChunk {
    @Override
    public void write(DataOutput dataOutput) {
        Writes writes = new Writes(dataOutput);
        
        MPCore.renderer.write(writes);
    }

    @Override
    public void read(DataInput dataInput) {
        Reads reads = new Reads(dataInput);
        
        MPCore.renderer.read(reads);
    }

    @Override
    public boolean shouldWrite() {
        return true;
    }

    @Override
    public boolean writeNet() {
        return true;
    }
}
