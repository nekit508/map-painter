package com.github.nekit508.mappainter.gui.utils;

import arc.util.Disposable;

public interface ReusableStream<T> extends Disposable {
    T readNextObject() throws EOSException;

    T get();
    T next();

    void redo();
    void redo(int num);

    void saveState();
    void redoState();
    void disposeState();

    int getPos();

    class EOSException extends RuntimeException {
        public EOSException() {
            super("End of stream.");
        }
    }
}
