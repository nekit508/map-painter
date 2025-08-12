package com.github.nekit508.mappainter.ui;

import com.github.nekit508.mappainter.ui.dialogs.KeybindsDialog;
import com.github.nekit508.mappainter.ui.dialogs.ObjectEditorDialog;

public class MPUI {
    public static ObjectEditorDialog objectEditorDialog;
    public static KeybindsDialog keybindsDialog;

    public static void init() {
        objectEditorDialog = new ObjectEditorDialog("@object-editor-fragment");
        keybindsDialog = new KeybindsDialog();
    }
}
