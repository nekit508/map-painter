package com.github.nekit508.mappainter.graphics;

import arc.func.Cons;
import arc.graphics.g2d.SpriteBatch;
import arc.graphics.g2d.TextureRegion;
import arc.struct.IntSeq;
import arc.struct.Seq;
import com.github.bsideup.jabel.Desugar;
import com.github.nekit508.Pair;

public class SpriteBatchWrapper extends SpriteBatch {
    public Seq<Seq<DrawRecord>> seqs = new Seq<>();
    public Seq<Pair<Float, Float>> ranges = new Seq<>();
    public IntSeq nums = new IntSeq();

    /** Inclusive. */
    protected boolean capturing = false;

    protected Pair<Float, Float> tmp$Pair = new Pair<>(0f, 0f);

    /** Begins capturing draws between two z layers. Inclusive. */
    public Seq<DrawRecord> bind(float minZ, float maxZ) {
        var ind = ranges.indexOf((Pair<Float, Float>) tmp$Pair.set(minZ, maxZ));

        if (ind != -1) {
            nums.incr(ind, 1);
            return seqs.get(ind);
        } else {
            var out = new Seq<DrawRecord>();
            seqs.add(out);
            nums.add(1);
            ranges.add(new Pair<>(minZ, maxZ));
            return out;
        }
    }

    /** Stops capturing draws between two z layers. Inclusive. */
    public void unbind(float minZ, float maxZ) {
        var ind = ranges.indexOf((Pair<Float, Float>) tmp$Pair.set(minZ, maxZ));

        if (ind == -1)
            throw new IndexOutOfBoundsException("Specified range is not captured. [" + minZ + ":" + maxZ + "].");

        var num = nums.get(ind) - 1;
        if (num == 0) {
            ranges.remove(ind);
            seqs.remove(ind);
            nums.removeIndex(ind);
        } else {
            nums.set(ind, num);
        }
    }

    public void clear() {
        seqs.each(Seq::clear);
    }

    public void begin() {
        begin(false);
    }

    public void begin(boolean clear) {
        if (clear) clear();
        capturing = true;
    }

    public void end() {
        capturing = false;
    }

    protected void saveInValidRanges(Cons<Seq<DrawRecord>> cons) {
        for (int i = 0; i < ranges.size; i++) {
            var range = ranges.get(i);

            if (range.left <= z && z <= range.right)
                cons.get(seqs.get(i));
        }
    }

    @Override
    protected void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height, float rotation) {
        if (capturing)
            saveInValidRanges(seq -> seq.add(new DrawRecord(region, x, y, originX, originY, width, height, rotation, z)));
        super.draw(region, x, y, originX, originY, width, height, rotation);
    }

    @Desugar
    public record DrawRecord(TextureRegion region, float x, float y, float originX, float originY, float width, float height, float rotation, float z) {
        @Override
        public String toString() {
            return "DrawRecord{" +
                    "region=" + region +
                    ", x=" + x +
                    ", y=" + y +
                    ", originX=" + originX +
                    ", originY=" + originY +
                    ", width=" + width +
                    ", height=" + height +
                    ", rotation=" + rotation +
                    ", z=" + z +
                    '}';
        }
    }
}
