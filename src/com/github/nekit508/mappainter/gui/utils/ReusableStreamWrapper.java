package com.github.nekit508.mappainter.gui.utils;

public class ReusableStreamWrapper<T> implements ReusableStream<T> {
    public ReusableStream<T> stream;

    public ReusableStreamWrapper(ReusableStream<T> stream) {
        this.stream = stream;
    }

    @Override
    public T readNextObject() throws EOSException {
        return stream.readNextObject();
    }

    @Override
    public T get() {
        return stream.get();
    }

    @Override
    public T next() {
        return stream.next();
    }

    @Override
    public void redo() {
        stream.redo();
    }

    @Override
    public void redo(int num) {
        stream.redo(num);
    }

    @Override
    public void saveState() {
        stream.saveState();
    }

    @Override
    public void redoState() {
        stream.redoState();
    }

    @Override
    public void disposeState() {
        stream.disposeState();
    }

    @Override
    public int getPos() {
        return stream.getPos();
    }

    @Override
    public void dispose() {
        stream.disposeState();
    }
}
