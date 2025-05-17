package com.github.nekit508.mappainter.core;

import arc.Core;
import arc.util.Log;
import com.github.nekit508.betterfloors.core.BetterFloorsCore;
import com.github.nekit508.mappainter.control.MPControl;
import com.github.nekit508.mappainter.files.InternalFileTree;
import com.github.nekit508.mappainter.graphics.MPRenderer;
import com.github.nekit508.mappainter.graphics.figure.*;
import com.github.nekit508.mappainter.net.packets.MPPackets;
import com.github.nekit508.mappainter.ui.MPUI;
import mindustry.Vars;
import mindustry.io.SaveVersion;
import mindustry.mod.Mod;
import mindustry.type.Item;

import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.jar.JarFile;

public class MPCore extends Mod {
    public static MPRenderer renderer;
    public static MPControl control;
    public static MPFiguresManager figuresManager;

    public static InternalFileTree files = new InternalFileTree(MPCore.class);

    public static BetterFloorsCore betterFloorsCore;
    public static boolean loadBetterFloors = false;

    public MPCore() {
        if (loadBetterFloors)
            betterFloorsCore = new BetterFloorsCore();

        figuresManager = new MPFiguresManager();
        SaveVersion.addCustomChunk("map-painter-figures-custom-chunk", figuresManager);

        MPPackets.init();

        if (System.getProperty("com-github-nekit508-mp-loaded", "false").equals("false") && false) {
            var thread = new Thread(new ThreadGroup(Thread.currentThread().getThreadGroup().getParent(), "New main group"), () -> {
                try {
                    System.setProperty("loaded", "true");
                    Thread.sleep(5000);

                    var url = Vars.class.getProtectionDomain().getCodeSource().getLocation();
                    var jar = new JarFile(Paths.get(url.toURI()).toString());
                    var mainClass = jar.getManifest().getMainAttributes().getValue("Main-Class");

                    Log.info(mainClass);

                    var loader = new URLClassLoader(new java.net.URL[]{url});
                    var method = loader.loadClass(mainClass).getMethod("main", String[].class);
                    method.invoke(null, (Object) new String[0]);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            thread.setDaemon(false);
            thread.setName("New main thread");
            thread.start();

            Thread.currentThread().getThreadGroup().interrupt();
            Core.app.exit();
        }
    }

    @Override
    public void loadContent() {
        if (loadBetterFloors)
            betterFloorsCore.loadContent();

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

        renderer = new MPRenderer();
        control = new MPControl();

        control.init();
    }
}
