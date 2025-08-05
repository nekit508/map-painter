package com.github.nekit508.mappainter.gui.compiletime;

import arc.files.Fi;
import arc.util.Disposable;
import com.github.nekit508.mappainter.gui.utils.ReusableCharStream;
import com.github.nekit508.mappainter.gui.utils.ReusableStream;

import java.io.InputStreamReader;

public class FileCompileSource implements CompileSource, Disposable {
    public Fi file;

    public FileCompileSource(Fi file) {
        this.file = file;
    }

    @Override
    public ReusableStream<Character> getInputStream() {
        return new ReusableCharStream(new InputStreamReader(file.read()), 512);
    }

    @Override
    public void dispose() {
        file = null;
    }
}
