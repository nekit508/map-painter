package com.github.nekit508.betterfloors.tools;

import arc.files.Fi;
import arc.graphics.Color;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Nullable;
import arc.util.Strings;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.ctype.ContentType;
import mindustry.world.Block;
import mindustry.world.blocks.environment.Floor;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class BFPreset {
    public static String readableExtension = "prst", binaryExtensions = "prstb";
    public static int version = 3;

    public ObjectMap<Color, Floor> floors = new ObjectMap<>();

    public void fillWithCurrentContent() {
        Seq<Block> floors = Vars.content.blocks().select(b -> b.getClass() == Floor.class || (b.getClass().getSuperclass() == Floor.class && b.getClass().isAnonymousClass()));
        for (int i = 0; i < floors.size; i++) {
            if (i > 0xffffff-1) {
                Log.warn("Too muсh floors (@ > @) - preset is not full.", i, 0xffffff-1);
                break;
            }
            this.floors.put(new Color().rgb888(i+1), (Floor) floors.get(i));
        }
    }

    public void read(Reads reads) {
        floors.clear();

        int fileVersion = reads.i();

        int size = reads.i();
        for (int i = 0; i < size; i++) {
            Color key = new Color().rgb888(reads.i());
            Floor value = Vars.content.getByName(ContentType.block, reads.str());
            floors.put(key, value);
        }
    }

    public void write(Writes writes) {
        Seq<Color> keys = floors.keys().toSeq();

        writes.i(version);

        writes.i(keys.size);
        for (int i = 0; i < keys.size; i++) {
            writes.i(keys.get(i).rgb888());
            writes.str(floors.get(keys.get(i)).name);
        }
    }

    public void writeReadable(@Nullable Fi fi) {
        if (fi == null) {
            Fi dir = Vars.dataDirectory.child("presets");
            int id = 0;
            while (fi == null || fi.exists()) fi = dir.child("preset-" + id++ + "." + readableExtension);
        }

        try (OutputStream s = fi.write()) {
            OutputStreamWriter output = new OutputStreamWriter(s);;

            output.append("file format version " + version + "\n");
            output.append("#hexcolor - floor-name\n");

            for (ObjectMap.Entry<Color, Floor> entry : floors)
                output.append(Strings.format("#@ - @\n", toHex(entry.key.rgb888()), entry.value.name));
            output.close();

            Writes writes = fi.sibling(fi.nameWithoutExtension() + "." + binaryExtensions).writes();
            write(writes);
            writes.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toHex(int color) {
        int r = (color & 0x0000ff);
        int g = (color & 0x00ff00) >>> 8;
        int b = (color & 0xff0000) >>> 16;

        return new String(new char[]{
                Character.forDigit((r & 0xf0) >>> 4, 16), Character.forDigit(r & 0xf, 16),
                Character.forDigit((g & 0xf0) >>> 4, 16), Character.forDigit(g & 0xf, 16),
                Character.forDigit((b & 0xf0) >>> 4, 16), Character.forDigit(b & 0xf, 16),
        });
    }
}
