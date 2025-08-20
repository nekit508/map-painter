package com.github.nekit508.mappainter.gui.n;

import com.github.nekit508.plcf.lang.Tree;
import com.github.nekit508.plcf.lang.TreeKind;
import com.github.nekit508.plcf.lang.TreeWalker;

public abstract class GUITree extends Tree {
    public GUITree(TreeKind kind) {
        super(kind);
    }

    public static class Function extends GUITree {

        public Function(TreeKind kind) {
            super(kind);
        }

        @Override
        public void accept(TreeWalker<?> analyzer) {

        }
    }

    public static class Create extends GUITree {
        public Create(TreeKind kind) {
            super(kind);
        }

        @Override
        public void accept(TreeWalker<?> analyzer) {

        }
    }
}
