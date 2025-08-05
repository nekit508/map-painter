package com.github.nekit508.mappainter.gui;

import arc.struct.Seq;

public abstract class TreeAnalyzer<T extends Context> {
    public Seq<Tree> depthStack = new Seq<>();
    public T context;

    public TreeAnalyzer(T context) {
        this.context = context;
    }

    public void assignment(Tree.Assignment tree) {

    }

    public void defaultSettings(Tree.DefaultsSettings tree) {

    }

    public void elementDecl(Tree.ElementDecl tree) {

    }

    public void methodExec(Tree.MethodExec tree) {

    }

    public void elementBody(Tree.ElementBody tree) {

    }

    public void booleanValue(Tree.BooleanValue tree) {

    }

    public void stringValue(Tree.StringValue tree) {

    }

    public void numericValue(Tree.NumericValue tree) {

    }

    public void ident(Tree.Ident tree) {

    }

    public void funcDecl(Tree.FuncDecl tree) {

    }

    public void funcExec(Tree.FuncExec funcExec) {

    }

    public void unit(Tree.Unit unit) {

    }

    public void enter(Tree tree) {
        depthStack.add(tree);
    }

    public void exit(Tree tree) {
        depthStack.pop();
    }

    public <T extends Tree> T as(Tree tree) {
        return (T) tree;
    }
}
