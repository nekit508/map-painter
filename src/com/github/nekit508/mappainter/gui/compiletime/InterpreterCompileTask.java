package com.github.nekit508.mappainter.gui.compiletime;

import com.github.nekit508.mappainter.gui.InterpreterContext;
import com.github.nekit508.mappainter.gui.exceptions.TaskException;
import com.github.nekit508.mappainter.gui.runtime.Executable;
import com.github.nekit508.mappainter.gui.runtime.InterpreterExecutable;

public class InterpreterCompileTask extends CompileTask<InterpreterContext> {
    public InterpreterCompileTask(CompileSource compileSource, InterpreterContext context) {
        super(compileSource, context);
    }

    @Override
    public void run() throws TaskException {
        try {
            var unit = context.getParser(context.getTokenizer(compileSource.getInputStream())).parseCompileSource();
            var analyzers = context.getAnalyzers();
            analyzers.each(unit::accept);
            var executables = context.getCompiler().compile(unit);

            for (Executable<?> executable : executables) {
                if (executable instanceof InterpreterExecutable exec)
                    context.addExecutable(exec);
                else
                    throw new IllegalArgumentException("Wrong executable type. Required <? extends " + InterpreterExecutable.class.getCanonicalName() +
                        ">. but " + executable.getClass().getCanonicalName() + " provided.");
            }
        } catch (Throwable e) {
            throw new TaskException(e);
        }
    }
}
