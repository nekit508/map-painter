package com.github.nekit508.mappainter.core;

import arc.util.Log;
import com.github.nekit508.betterfloors.core.BetterFloorsCore;
import com.github.nekit508.mappainter.content.MPDistribution;
import com.github.nekit508.mappainter.content.MPFx;
import com.github.nekit508.mappainter.content.MPOilProduction;
import com.github.nekit508.mappainter.control.MPControl;
import com.github.nekit508.mappainter.control.MPAxisBindings;
import com.github.nekit508.mappainter.control.MPKeyBindings;
import com.github.nekit508.mappainter.files.InternalFileTree;
import com.github.nekit508.mappainter.graphics.MPRenderer;
import com.github.nekit508.mappainter.graphics.figure.*;
import com.github.nekit508.mappainter.gui.InterpreterContext;
import com.github.nekit508.mappainter.gui.compiletime.CompileTask;
import com.github.nekit508.mappainter.gui.compiletime.FileCompileSource;
import com.github.nekit508.mappainter.gui.exceptions.TaskException;
import com.github.nekit508.mappainter.net.packets.MPPackets;
import com.github.nekit508.mappainter.register.BaseRegistries;
import com.github.nekit508.mappainter.ui.MPUI;
import com.github.nekit508.mappainter.world.blocks.misc.Radar;
import mindustry.content.Items;
import mindustry.io.SaveVersion;
import mindustry.mod.Mod;
import mindustry.type.Category;
import mindustry.type.Item;
import mindustry.type.ItemStack;

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

        MPDistribution.init();
        MPOilProduction.init();

        BaseRegistries.blocks.register("radar", () -> new Radar("radar"){{
            size = 3;
            health = 180;
            hasPower = true;
            consumePower(360f / 60f);

            requirements(Category.effect, ItemStack.with(Items.silicon, 140, Items.lead, 60, Items.titanium, 30, Items.copper, 85));
        }});

        BaseRegistries.items.resolve();
        BaseRegistries.liquids.resolve();
        BaseRegistries.blocks.resolve();
        BaseRegistries.sectorPresets.resolve();
        BaseRegistries.planets.resolve();


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

        MPUI.keybindsDialog.addKeybindings(MPKeyBindings.values());
        MPUI.keybindsDialog.addKeybindings(MPAxisBindings.values());

        renderer = new MPRenderer();

        control.init();
    }
}
