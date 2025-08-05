package com.github.nekit508.mappainter.gui.runtime;

import com.github.nekit508.mappainter.gui.InterpreterContext;

import java.io.ByteArrayInputStream;

public class InterpreterExecutable extends Executable<InterpreterContext> {
    public byte[] bytes;

    public InterpreterExecutable(InterpreterContext context, byte[] bytes, String name) {
        super(context, name);
        this.bytes = bytes;
    }

    @Override
    public void execute(Object object) {
        context.interpreter.objectsStack.add(object);
        execute();
    }

    public void execute() {
        context.interpreter.execute(new ByteArrayInputStream(bytes));
    }
}
