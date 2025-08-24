package com.github.nekit508.mappainter.gui.n;

import com.github.nekit508.plcf.lang.TreeKind;

public enum GUITreeKinds implements TreeKind {
    FILE,
    FUNCTION,

    IDENT,
    CONSTANT,

    OBJECT_CLOSURE,
    CELL_CLOSURE,

    METHOD_CALL,
    ASSIGNMENT
}
