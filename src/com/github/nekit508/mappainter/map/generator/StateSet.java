package com.github.nekit508.mappainter.map.generator;

import arc.math.Mathf;

public class StateSet {
    public StateType[] possibilities;

    public StateSet(StateType... possibilities) {
        this.possibilities = possibilities;
    }

    public StateType getRandom() {
        return possibilities[Mathf.rand.random(0, possibilities.length - 1)];
    }
}
