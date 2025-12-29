package com.github.nekit508.mappainter.map.generator;

import arc.func.Func2;
import com.github.nekit508.mappainter.utils.ClipType;

public class WorldOverlay<T> {

    public int width, height;
    public ClipType clip = ClipType.clamp;

    public T[][] objects;

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void fill(Func2<Integer, Integer, T> prov) {
        objects = (T[][]) new Object[width][height];

        for (int i = 0; i < width; i++) {
            objects[i] = (T[]) new Object[height];

            for (int j = 0; j < height; j++)
                objects[i][j] = prov.get(i, j);
        }
    }

    public T get(int x, int y) {
        int tx = clip.get(x, width);
        int ty = clip.get(y, height);

        //Log.info("@:@ @:@ \n@:@", x, y, width, height, tx, ty);

        return objects[tx][ty];
    }

    public T set(T newObject, int x, int y) {
        int tx = clip.get(x, width);
        int ty = clip.get(y, height);

        T old = get(tx, ty);
        objects[tx][ty] = newObject;

        return old;
    }
}
