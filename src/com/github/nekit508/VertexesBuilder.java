package com.github.nekit508;

import arc.struct.FloatSeq;
import arc.struct.ShortSeq;

public class VertexesBuilder {
    protected final FloatSeq vertexes = new FloatSeq();
    protected final int vertexSize;

    public VertexesBuilder(int vertexSize) {
        this.vertexSize = vertexSize;
    }

    public void consumeVertexes(float[]... vertexes) {
        for (float[] vertex : vertexes)
            consumeVertex(vertex);
    }

    public void consumeVertex(float[] vertex) {
        if (vertex.length != vertexSize)
            throw new IllegalArgumentException("Consumed vertex size does not equals to builder's vertexSize. " + vertex.length + " != " + vertexSize + ".");
        vertexes.addAll(vertex);
    }

    public Pair<float[], short[]> build() {
        var processedVertexes = new FloatSeq();
        var indices = new ShortSeq();

        vertexes: for (int i = 0; i < vertexes.size / vertexSize; i++) {
            // check if any vertex with same data exists
            indices: for (int j = 0; j < processedVertexes.size / vertexSize; j++) {
                // check data equality
                for (int k = 0; k < vertexSize; k++) {
                    if (vertexes.get(i*vertexSize + k) != processedVertexes.get(j*vertexSize + k))
                        continue indices; // skip at any data mismatch
                }

                indices.add(j);

                continue vertexes;
            }
            // next code executes if vertex's data is unique

            // add vertex to output vertexes list
            for (int j = 0; j < vertexSize; j++)
                processedVertexes.add(vertexes.get(i+j));

            // save newly added vertex index into indices list
            indices.add(processedVertexes.size);
        }

        return new Pair<>(processedVertexes.items, indices.items);
    }
}
