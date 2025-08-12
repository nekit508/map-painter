package com.github.nekit508.mappainter.gui.utils;

import arc.struct.IntSeq;
import arc.struct.Seq;

public abstract class ReusableStreamAbstractImpl<T> implements ReusableStream<T> {
    public final Seq<T> obtainedObjects = new Seq<>();
    public final IntSeq stateStack = new IntSeq();

    public int currentPos = -1;

    @Override
    public T get() {
        return obtainedObjects.get(currentPos);
    }

    @Override
    public T next() {
        currentPos++;

        if (obtainedObjects.size == currentPos)
            obtainedObjects.add(readNextObject());

        return get();
    }

    @Override
    public void redo() {
        currentPos -= 1;
    }

    @Override
    public void redo(int num) {
        for (int i = 0; i < num; i++)
            redo();
    }

    @Override
    public void saveState() {
        stateStack.add(currentPos);
    }

    @Override
    public void redoState() {
        redo(currentPos - stateStack.pop());
    }

    @Override
    public void disposeState() {
        stateStack.pop();
    }

    @Override
    public void dispose() {
        stateStack.clear();
        obtainedObjects.clear();
    }

    @Override
    public int getPos() {
        return currentPos;
    }
}
