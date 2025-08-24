package com.github.nekit508.mappainter.gui.n;

import arc.struct.Seq;
import arc.util.Nullable;
import com.github.nekit508.plcf.lang.Tree;
import com.github.nekit508.plcf.lang.TreeKind;
import com.github.nekit508.plcf.lang.TreeWalker;

public abstract class GUITree extends Tree {
    public GUITree(TreeKind kind) {
        super(kind);
    }

    public static class File extends GUITree {
        public Seq<Function> functions;

        public File(TreeKind kind, Seq<Function> functions) {
            super(kind);
            this.functions = functions;
        }

        @Override
        public void accept(TreeWalker<?> analyzer) {
            analyzer.enter(this);
            functions.each(t -> t.accept(analyzer));
            analyzer.exit(this);
        }
    }

    public static class Function extends GUITree {
        public String name;
        public Seq<Closure> closures;
        public Seq<String> params;

        public Function(TreeKind kind, String name, Seq<String> params, Seq<Closure> closures) {
            super(kind);
            this.name = name;
            this.params = params;
            this.closures = closures;
        }

        @Override
        public void accept(TreeWalker<?> analyzer) {
            analyzer.enter(this);
            analyzer.exit(this);
        }
    }

    public static abstract class Value extends GUITree {
        public Value(TreeKind kind) {
            super(kind);
        }
    }

    public static class Ident extends Value {
        public String name;
        public @Nullable Ident target;

        public Ident(TreeKind kind, String name, @Nullable Ident target) {
            super(kind);
            this.name = name;
            this.target = target;
        }

        @Override
        public void accept(TreeWalker<?> analyzer) {
            analyzer.enter(this);
            if (target != null)
                target.accept(analyzer);
            analyzer.exit(this);
        }
    }

    public static abstract class Statement extends Value {
        public Statement(TreeKind kind) {
            super(kind);
        }
    }

    public static class ConstantValue extends Value {
        public Object value;

        public ConstantValue(TreeKind kind, Object value) {
            super(kind);
            this.value = value;
        }

        @Override
        public void accept(TreeWalker<?> analyzer) {
            analyzer.enter(this);
            analyzer.exit(this);
        }
    }

    public static class Assignment extends Statement {
        public String fieldName;
        public Value value;

        public Assignment(TreeKind kind, String fieldName, Value value) {
            super(kind);
            this.fieldName = fieldName;
            this.value = value;
        }

        @Override
        public void accept(TreeWalker<?> analyzer) {
            analyzer.enter(this);
            value.accept(analyzer);
            analyzer.exit(this);
        }
    }

    public static class MethodCall extends Statement {
        public String name;
        public Seq<Value> params;
        public Seq<Closure> closures;

        public MethodCall(TreeKind kind, String name, Seq<Value> params, Seq<Closure> closures) {
            super(kind);
            this.name = name;
            this.params = params;
            this.closures = closures;
        }

        @Override
        public void accept(TreeWalker<?> analyzer) {
            analyzer.enter(this);
            params.each(t -> t.accept(analyzer));
            analyzer.exit(this);
        }
    }

    public static abstract class Closure extends GUITree {
        public Seq<Statement> statements;

        public Closure(TreeKind kind, Seq<Statement> statements) {
            super(kind);
            this.statements = statements;
        }

        @Override
        public void accept(TreeWalker<?> analyzer) {
            statements.each(t -> t.accept(analyzer));
        }
    }

    public static class ObjectClosure extends Closure {
        public ObjectClosure(TreeKind kind, Seq<Statement> statements) {
            super(kind, statements);
        }

        @Override
        public void accept(TreeWalker<?> analyzer) {
            analyzer.enter(this);
            super.accept(analyzer);
            analyzer.exit(this);
        }
    }

    public static class CellClosure extends Closure {
        public CellClosure(TreeKind kind, Seq<Statement> statements) {
            super(kind, statements);
        }

        @Override
        public void accept(TreeWalker<?> analyzer) {
            analyzer.enter(this);
            super.accept(analyzer);
            analyzer.exit(this);
        }
    }
}
