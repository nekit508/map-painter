package com.github.nekit508;

import java.util.Objects;

public class Pair<L, R> {
    public L left;
    public R right;

    public Pair(L left, R right) {
        set(left, right);
    }

    public <O extends Pair<L, R>> O set(L left, R right) {
        this.left = left;
        this.right = right;
        return (O) this;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof Pair other && Objects.equals(left, other.left) && Objects.equals(right, other.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }
}
