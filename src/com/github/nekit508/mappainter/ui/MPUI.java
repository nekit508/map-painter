package com.github.nekit508.mappainter.ui;

import com.github.nekit508.mappainter.ui.dialogs.KeybindsDialog;
import com.github.nekit508.mappainter.ui.dialogs.ObjectEditorDialog;
import com.github.nekit508.mappainter.ui.dialogs.SpriteReloaderDialog;

public class MPUI {
    public static ObjectEditorDialog objectEditorDialog;
    public static KeybindsDialog keybindsDialog;
    public static SpriteReloaderDialog spriteReloaderDialog;

    public static void init() {
        objectEditorDialog = new ObjectEditorDialog("@object-editor-fragment");
        keybindsDialog = new KeybindsDialog("@map-painter-keybindings-dialog");
        spriteReloaderDialog = new SpriteReloaderDialog("@map-painter-sprite-reloader");
    }
}
