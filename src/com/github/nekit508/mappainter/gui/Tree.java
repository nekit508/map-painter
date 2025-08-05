package com.github.nekit508.mappainter.gui;

import arc.struct.Seq;

public abstract class Tree {
    public Kind kind;
    public Tree(Kind kind) {
        this.kind = kind;
    }

    public abstract void accept(TreeAnalyzer analyzer);

    public static class Unit extends Tree {
        public Seq<FuncDecl> members;

        public Unit(Kind kind, Seq<FuncDecl> members) {
            super(kind);
            this.members = members;
        }

        @Override
        public void accept(TreeAnalyzer analyzer) {
            analyzer.enter(this);
            members.each(t -> t.accept(analyzer));
            analyzer.unit(this);
            analyzer.exit(this);
        }
    }

    public static class Ident extends Value {
        public String literal;

        public Ident(Kind kind, String literal) {
            super(kind);
            this.literal = literal;
        }

        @Override
        public void accept(TreeAnalyzer analyzer) {
            analyzer.enter(this);
            analyzer.ident(this);
            analyzer.exit(this);
        }
    }

    public static class Assignment extends Tree {
        public Ident key;
        public Value value;

        public Assignment(Kind kind, Ident key, Value value) {
            super(kind);
            this.value = value;
            this.key = key;
        }

        @Override
        public void accept(TreeAnalyzer analyzer) {
            analyzer.enter(this);

            key.accept(analyzer);
            value.accept(analyzer);

            analyzer.assignment(this);

            analyzer.exit(this);
        }
    }

    public static class ElementBody extends Tree {
        public Seq<Tree> members;

        public ElementBody(Kind kind, Seq<Tree> members) {
            super(kind);
            this.members = members;
        }

        @Override
        public void accept(TreeAnalyzer analyzer) {
            analyzer.enter(this);
            members.each(t -> t.accept(analyzer));
            analyzer.elementBody(this);
            analyzer.exit(this);
        }
    }

    public static class ElementDecl extends Tree {
        public Ident method;
        public Seq<Value> args;
        public ElementBody body;
        public Seq<MethodExec> cellMethodExecutions;

        public ElementDecl(Kind kind, Ident method, ElementBody body, Seq<Value> args, Seq<MethodExec> cellMethodExecutions) {
            super(kind);
            this.body = body;
            this.method = method;
            this.args = args;
            this.cellMethodExecutions = cellMethodExecutions;
        }

        @Override
        public void accept(TreeAnalyzer analyzer) {
            analyzer.enter(this);
            cellMethodExecutions.each(t -> t.accept(analyzer));
            args.each(a -> a.accept(analyzer));
            method.accept(analyzer);
            body.accept(analyzer);

            analyzer.elementDecl(this);
            analyzer.exit(this);
        }
    }

    public static class DefaultsSettings extends Tree {
        public Seq<MethodExec> methodExecs;

        public DefaultsSettings(Kind kind, Seq<MethodExec> methodExecs) {
            super(kind);
            this.methodExecs = methodExecs;
        }

        @Override
        public void accept(TreeAnalyzer analyzer) {
            analyzer.enter(this);
            methodExecs.each(t -> t.accept(analyzer));

            analyzer.defaultSettings(this);
            analyzer.exit(this);
        }
    }

    public static abstract class Value extends Tree {
        public Value(Kind kind) {
            super(kind);
        }
    }

    public static class BooleanValue extends Value {
        public boolean value;

        public BooleanValue(Kind kind, boolean value) {
            super(kind);
            this.value = value;
        }

        @Override
        public void accept(TreeAnalyzer analyzer) {
            analyzer.enter(this);
            analyzer.booleanValue(this);
            analyzer.exit(this);
        }
    }

    public static class NumericValue extends Value {
        public Object value;

        public NumericValue(Kind kind, Object value) {
            super(kind);
            this.value = value;
        }

        @Override
        public void accept(TreeAnalyzer analyzer) {
            analyzer.enter(this);
            analyzer.numericValue(this);
            analyzer.exit(this);
        }
    }

    public static class StringValue extends Value {
        public String value;

        public StringValue(Kind kind, String value) {
            super(kind);
            this.value = value;
        }

        @Override
        public void accept(TreeAnalyzer analyzer) {
            analyzer.enter(this);
            analyzer.stringValue(this);
            analyzer.exit(this);
        }
    }

    public static class MethodExec extends Tree {
        public Ident method;
        public Seq<Value> params;

        public MethodExec(Kind kind, Ident method, Seq<Value> params) {
            super(kind);
            this.method = method;
            this.params = params;
        }

        @Override
        public void accept(TreeAnalyzer analyzer) {
            analyzer.enter(this);

            params.each(t -> t.accept(analyzer));
            method.accept(analyzer);

            analyzer.methodExec(this);

            analyzer.exit(this);
        }
    }

    public static class FuncDecl extends Tree {
        public Ident name;
        public ElementBody body;

        public FuncDecl(Kind kind, Ident name, ElementBody body) {
            super(kind);
            this.name = name;
            this.body = body;
        }

        @Override
        public void accept(TreeAnalyzer analyzer) {
            analyzer.enter(this);
            body.accept(analyzer);
            analyzer.funcDecl(this);
            analyzer.exit(this);
        }
    }

    public static class FuncExec extends Tree {
        public Ident name;

        public FuncExec(Kind kind, Ident name) {
            super(kind);
            this.name = name;
        }

        @Override
        public void accept(TreeAnalyzer analyzer) {
            analyzer.enter(this);
            analyzer.funcExec(this);
            analyzer.exit(this);
        }
    }

    public enum Kind {
        IDENT,
        ELEMENT_DECL,
        BOOLEAN_VALUE,
        NUMERIC_VALUE,
        STRING_VALUE,
        ASSIGNMENT,
        ELEMENT_BODY,
        METHOD_EXEC,
        DEFAULTS_SETTINGS,
        FUNC_DECL,
        FUNC_EXEC,
        UNIT
    }
}
