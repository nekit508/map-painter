package com.github.nekit508.mappainter.struct;

import arc.func.Cons;
import arc.struct.Seq;
import arc.util.Disposable;

public class GraphNode<T extends GraphNode> {
    public Seq<T> children = new Seq<>(), parents = new Seq<>();

    public void add(T child) {
        children.add(child);
        child.parents.add(this);
    }

    public void remove(T child) {
        children.remove(child);
        child.parents.remove(this);
    }

    public void accept(GraphNodeAcceptor<T> acceptor) {
        acceptor.get((T) this);
        for (T child : children)
            child.accept(acceptor);
    }

    public void acceptUp(GraphNodeAcceptor<T> acceptor) {
        acceptor.get((T) this);
        for (T parent : parents)
            parent.accept(acceptor);
    }

    public static abstract class GraphNodeAcceptor<T extends GraphNode> implements Cons<T>, Disposable {
        public Seq<T> accepted = new Seq<>();

        @Override
        public void get(T node) {
            if (accepted.contains(node)) return;
            accept(node);
            accepted.add(node);
        }

        public abstract void accept(T node);

        @Override
        public void dispose() {
            accepted.clear();
        }
    }
}
