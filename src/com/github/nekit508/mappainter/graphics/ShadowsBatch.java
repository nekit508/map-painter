package com.github.nekit508.mappainter.graphics;

import arc.graphics.Mesh;
import arc.graphics.VertexAttribute;
import arc.graphics.g2d.SpriteBatch;

public class ShadowsBatch extends SpriteBatch {
    public ShadowsBatch(int size) {
        super(size, MPShaders.shadowShader);
        mesh.dispose();

        if (size > 0) {
            mesh = new Mesh(true, false, size * 4, size * 6,
                    VertexAttribute.position,
                    MPVertexAttributes.height,
                    VertexAttribute.texCoords,
                    VertexAttribute.mixColor
            );

            int len = size * 6;
            short[] indices = new short[len];
            short j = 0;
            for(int i = 0; i < len; i += 6, j += 4){
                indices[i] = j;
                indices[i + 1] = (short)(j + 1);
                indices[i + 2] = (short)(j + 2);
                indices[i + 3] = (short)(j + 2);
                indices[i + 4] = (short)(j + 3);
                indices[i + 5] = j;
            }
            mesh.setIndices(indices);
            mesh.getVerticesBuffer().position(0);
            mesh.getVerticesBuffer().limit(mesh.getVerticesBuffer().capacity());

            mesh.getIndicesBuffer();
            buffer = mesh.getVerticesBuffer();
        } else {
            shader = null;
        }
    }
}
