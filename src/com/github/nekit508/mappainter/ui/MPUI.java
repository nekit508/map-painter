package com.github.nekit508.mappainter.ui;

import com.github.nekit508.mappainter.ui.fragments.ObjectEditorDialog;

public class MPUI {
    public static ObjectEditorDialog objectEditorFragment;

    public static void init() {
        objectEditorFragment = new ObjectEditorDialog("@object-editor-fragment");
    }
}
