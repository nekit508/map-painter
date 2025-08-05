package com.github.nekit508.mappainter.gui.compiletime;

import arc.struct.ByteSeq;
import arc.struct.Seq;
import arc.util.Pack;
import com.github.nekit508.mappainter.gui.InterpreterContext;
import com.github.nekit508.mappainter.gui.Tree;
import com.github.nekit508.mappainter.gui.runtime.Executable;
import com.github.nekit508.mappainter.gui.runtime.InterpreterExecutable;

/**
 * Actioned object is actually a stack, all set/get/call (except push/pop) interactions with it redirects to top of stack. <br>
 *
 * Bytecode:
 *
 * <li>
 * {@link InterpreterCompiler#getElement} - get object instruction start after going
 * element ident literal as string bytes.
 * Gets object from dynamic value provider by ident and stores it in actioned object.
 * </li>
 *
 * <li>
 * {@link InterpreterCompiler#setField} - set field instruction start after going
 * field ident literal as string bytes,
 * fields value type ({@link InterpreterCompiler#dynamicValueType} means that string is a name of dynamic reference),
 * byte representation of static value or ref to dynamic value provider as bytes of value ident string.
 * Sets fields of actioned object to value.
 * </li>
 *
 * <li>
 * {@link InterpreterCompiler#methodExecution} - method execution instruction start after going
 * method name ident literal  as string bytes,
 * parameters size as int32,
 * [value type ({@link InterpreterCompiler#dynamicValueType} means that string is a name of dynamic reference),
 * byte representation of static value or ref to dynamic value provider as bytes of value ident string] * parameters size.
 * Executes method of actioned object with specified parameters.
 * </li>
 *
 * <li>
 * {@link InterpreterCompiler#funcExecution} - function execution instruction start  after going
 * function name ident literal as string bytes.
 * Dynamically gets function and executes it's instructions.
 * </li>
 *
 * <li>
 * {@link InterpreterCompiler#elementDecl} - element declaration instruction start after going
 * declaration method ident literal as string bytes,
 * dynamic value provider name as bytes of value ident string where new object will be stored
 * instructions size as int32,
 * [instruction] * instructions size.
 * Creates new object and adds it to actioned object, creates new dynamic value provider with specified name and stores there created object,
 * pushes create object to stack, executes body instructions, pop stack, push cell of created object to stack, executes cell instructions, pop stack.
 * </li>
 *
 * <li>
 * {@link InterpreterCompiler#defaultsSettings} - defaults settings instruction start
 * instructions size as int32,
 * [instruction] * instructions size.
 * Gets `defaults` from actioned object and push it in stack, executes instructions, pop stack.
 * </li>
 */
public class InterpreterCompiler extends Compiler<InterpreterContext> {
    public ByteSeq data = new ByteSeq();

    public static byte getElement = 1, setField = 1, methodExecution = 3, funcExecution = 4, elementDecl = 5, defaultsSettings = 6;

    public static byte stringValueType = 3, booleanValueType = 4, numericValueType = 0, dynamicValueType = 2;
    public static byte byteN = 0, intN = 1, shortN = 2, longN = 3, floatN = 4, doubleN = 5;

    public InterpreterCompiler(InterpreterContext context) {
        super(context);
    }

    @Override
    public Seq<Executable<InterpreterContext>> compile(Tree.Unit tree) {
        return tree.members.map(this::compile);
    }

    public void put(String string) {
        var b = string.getBytes();
        put(b.length);
        put(b);
    }

    public void put(byte[] value) {
        data.addAll(value);
    }

    public void put(byte value) {
        data.add(value);
    }

    public void put(int value) {
        var b = new byte[4];
        Pack.bytes(value, b);
        put(b);
    }

    public void put(float value) {
        put(Float.floatToRawIntBits(value));
    }

    public InterpreterExecutable compile(Tree.FuncDecl decl) {
        data.clear();

        put(decl.body.members.size);
        decl.body.members.each(this::statement);

        return new InterpreterExecutable(context, data.toArray(), decl.name.literal);
    }

    public void statement(Tree tree) {
        if (tree.kind == Tree.Kind.ASSIGNMENT)
            setField((Tree.Assignment) tree);
        else if (tree.kind == Tree.Kind.FUNC_EXEC)
            funcExec((Tree.FuncExec) tree);
        else if (tree.kind == Tree.Kind.METHOD_EXEC)
            methodExec((Tree.MethodExec) tree);
        else if (tree.kind == Tree.Kind.ELEMENT_DECL)
            elementDecl((Tree.ElementDecl) tree);
        else if (tree.kind == Tree.Kind.DEFAULTS_SETTINGS)
            defaults((Tree.DefaultsSettings) tree);
    }

    public void defaults(Tree.DefaultsSettings defaults) {
        put(defaultsSettings);
        put(defaults.methodExecs.size);
        defaults.methodExecs.each(this::methodExec);
    }

    public void elementDecl(Tree.ElementDecl decl) {
        put(elementDecl);

        identLiteral(decl.method);
        put(decl.args.size);
        for (Tree.Value arg : decl.args) {
            value(arg);
        }

        put(decl.body.members.size);
        decl.body.members.each(this::statement);

        put(decl.cellMethodExecutions.size);
        decl.cellMethodExecutions.each(this::methodExec);
    }

    public void setField(Tree.Assignment assignment) {
        put(setField);
        identLiteral(assignment.key);
        value(assignment.value);
    }

    public void methodExec(Tree.MethodExec exec) {
        put(methodExecution);
        identLiteral(exec.method);
        put(exec.params.size);
        exec.params.each(this::value);
    }

    public void funcExec(Tree.FuncExec exec) {
        put(funcExecution);
        identLiteral(exec.name);
    }

    public void identLiteral(Tree.Ident ident){
        put(ident.literal);
    }

    public void value(Tree.Value value) {
        if (value.kind == Tree.Kind.IDENT) {
            put(dynamicValueType);
            getValueByRef((Tree.Ident) value);
        } else if (value.kind == Tree.Kind.NUMERIC_VALUE) {
            put(numericValueType);
            numeric((Tree.NumericValue) value);
        } else if (value.kind == Tree.Kind.STRING_VALUE) {
            put(stringValueType);
            string((Tree.StringValue) value);
        } else if (value.kind == Tree.Kind.BOOLEAN_VALUE) {
            put(booleanValueType);
            booleanV((Tree.BooleanValue) value);
        }
    }

    public void getValueByRef(Tree.Ident ident) {
        identLiteral(ident);
    }

    public void numeric(Tree.NumericValue value) {
        Object v = value.value;

        if (v instanceof Integer i) {
            put(intN);
            put(i);
        } else if (v instanceof Float f) {
            put(floatN);
            put(f);
        } else
            throw new IllegalArgumentException("Wrong numeric type provided " + v + ".");
    }

    public void booleanV(Tree.BooleanValue value) {
        put((byte) (value.value ? 1 : 0));
    }

    public void string(Tree.StringValue value) {
        put(value.value);
    }
}
