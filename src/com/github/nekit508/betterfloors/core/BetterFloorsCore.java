package com.github.nekit508.betterfloors.core;

import com.github.nekit508.betterfloors.graphics.BFRenderer;
import com.github.nekit508.betterfloors.ui.BFDialog;
import com.github.nekit508.mappainter.files.InternalFileTree;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.io.SaveVersion;
import mindustry.mod.Mod;

public class BetterFloorsCore extends Mod {
    public static BFChunk controller;
    public static BFRenderer renderer;

    public static BFDialog dialog;

    public static InternalFileTree files = new InternalFileTree(BetterFloorsCore.class);

    public BetterFloorsCore() {
        renderer = new BFRenderer();
        SaveVersion.addCustomChunk("better-floor-custom-chunk", controller = new BFChunk());
    }

    @Override
    public void loadContent() {
        renderer.load();
    }

    @Override
    public void init() {
        renderer.init();

        dialog = new BFDialog("@bfdialog");

        Vars.ui.menufrag.addButton("@bftools-menu-button", Icon.hammer, () -> {
            dialog.build();
            dialog.show();
        });
    }
}
