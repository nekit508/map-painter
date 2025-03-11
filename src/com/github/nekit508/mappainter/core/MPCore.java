package com.github.nekit508.mappainter.core;

import com.github.nekit508.mappainter.control.MPControl;
import com.github.nekit508.mappainter.files.InternalFileTree;
import com.github.nekit508.mappainter.graphics.MPRenderer;
import com.github.nekit508.mappainter.graphics.figure.*;
import com.github.nekit508.mappainter.io.MPCustomChunk;
import mindustry.io.SaveVersion;
import mindustry.mod.Mod;

public class MPCore extends Mod {
    public static MPRenderer renderer;
    public static MPControl control;
    public static MPCustomChunk figuresCustomChunk;

    public static InternalFileTree files = new InternalFileTree(MPCore.class);

    public MPCore() {
        figuresCustomChunk = new MPCustomChunk();
        SaveVersion.addCustomChunk("map-painter-figures-custom-chunk", figuresCustomChunk);
    }

    @Override
    public void init() {
        renderer = new MPRenderer();
        control = new MPControl();


        new LineFigureType("line");
        new HandWrittenFigureType("hand-written");
        new ArrowFigureType("arrow");
        new ImageFigureType("image");
        new CircleFigureType("circle");

        control.init();
    }
}
