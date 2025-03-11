package com.github.nekit508.mappainter.graphics.g2d;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.SpriteBatch;
import arc.graphics.g2d.TextureRegion;

public class MPDraw {
    private static final float[] vertices = new float[SpriteBatch.SPRITE_SIZE];
    
    public static void shapedRect(TextureRegion region, float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
        final float u = region.u;
        final float v = region.v2;
        final float u2 = region.u2;
        final float v2 = region.v;

        float color = Draw.getColor().toFloatBits();
        float mixColor = Draw.getMixColor().toFloatBits();
        vertices[0] = x1;
        vertices[1] = y1;
        vertices[2] = color;
        vertices[3] = u;
        vertices[4] = v;
        vertices[5] = mixColor;

        vertices[6] = x2;
        vertices[7] = y2;
        vertices[8] = color;
        vertices[9] = u;
        vertices[10] = v2;
        vertices[11] = mixColor;

        vertices[12] = x3;
        vertices[13] = y3;
        vertices[14] = color;
        vertices[15] = u2;
        vertices[16] = v2;
        vertices[17] = mixColor;

        vertices[18] = x4;
        vertices[19] = y4;
        vertices[20] = color;
        vertices[21] = u2;
        vertices[22] = v;
        vertices[23] = mixColor;

        Draw.vert(region.texture, vertices, 0, vertices.length);
    }
}
