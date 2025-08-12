package com.github.nekit508.mappainter.core;

import arc.util.Log;
import com.github.nekit508.betterfloors.core.BetterFloorsCore;
import com.github.nekit508.mappainter.content.MPFx;
import com.github.nekit508.mappainter.control.MPControl;
import com.github.nekit508.mappainter.control.keys.MPAxisBuildings;
import com.github.nekit508.mappainter.control.keys.MPKeyboardBindings;
import com.github.nekit508.mappainter.files.InternalFileTree;
import com.github.nekit508.mappainter.graphics.MPRenderer;
import com.github.nekit508.mappainter.graphics.figure.*;
import com.github.nekit508.mappainter.gui.InterpreterContext;
import com.github.nekit508.mappainter.gui.compiletime.CompileTask;
import com.github.nekit508.mappainter.gui.compiletime.FileCompileSource;
import com.github.nekit508.mappainter.gui.exceptions.TaskException;
import com.github.nekit508.mappainter.net.packets.MPPackets;
import com.github.nekit508.mappainter.ui.MPUI;
import mindustry.io.SaveVersion;
import mindustry.mod.Mod;
import mindustry.type.Item;

import java.util.Objects;

public class MPCore extends Mod {
    public static MPRenderer renderer;
    public static MPControl control;
    public static MPFiguresManager figuresManager;

    public static InternalFileTree files = new InternalFileTree(MPCore.class);

    public static BetterFloorsCore betterFloorsCore;
    public static boolean loadBetterFloors = false;

    public static MPCore instance;

    public MPCore() {
        instance = this;

        if (loadBetterFloors)
            betterFloorsCore = new BetterFloorsCore();

        figuresManager = new MPFiguresManager();
        SaveVersion.addCustomChunk("map-painter-figures-custom-chunk", figuresManager);

        var context = new InterpreterContext();
        files.child("gui").findAll(fi -> Objects.equals(fi.extension(), "gui")).map(fi -> (CompileTask<?>) context.getCompileTaskFor(new FileCompileSource(fi))).each(task -> {
            try {
                task.run();
            } catch (TaskException e) {
                Log.err(e);
            }
        });

        MPPackets.init();

        control = new MPControl();
    }

    @Override
    public void loadContent() {
        if (loadBetterFloors)
            betterFloorsCore.loadContent();

        MPFx.load();

        figuresManager.addType(new LineFigureType("line"));
        figuresManager.addType(new HandWrittenFigureType("hand-written"));
        figuresManager.addType(new ArrowFigureType("arrow"));
        figuresManager.addType(new ImageFigureType("image"));
        figuresManager.addType(new CircleFigureType("circle"));

        new Item("dummy-item"){
            {
                hidden = true;
            }

            @Override
            public void load() {
                figuresManager.figureTypes.each(FigureType::load);
            }
        };
    }

    @Override
    public void init() {
        if (loadBetterFloors)
            betterFloorsCore.init();

        MPUI.init();

        MPUI.keybindsDialog.addKeybindings(MPKeyboardBindings.values());
        MPUI.keybindsDialog.addKeybindings(MPAxisBuildings.values());

        renderer = new MPRenderer();

        control.init();
    }
}
