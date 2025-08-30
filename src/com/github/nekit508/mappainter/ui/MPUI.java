package com.github.nekit508.mappainter.ui;

import com.github.nekit508.emkb.ui.dialogs.KeybindsDialog;
import com.github.nekit508.mappainter.ui.dialogs.NormalMapTester;
import com.github.nekit508.mappainter.ui.dialogs.ObjectEditorDialog;
import com.github.nekit508.mappainter.ui.dialogs.SpriteReloaderDialog;

public class MPUI {
    public static ObjectEditorDialog objectEditorDialog;
    public static SpriteReloaderDialog spriteReloaderDialog;
    public static NormalMapTester normalMapTester;

    public static void init() {
        objectEditorDialog = new ObjectEditorDialog("@object-editor-fragment");
        spriteReloaderDialog = new SpriteReloaderDialog("@map-painter-sprite-reloader");
        normalMapTester = new NormalMapTester("@map-painter-normal-map-tester");
    }
}
