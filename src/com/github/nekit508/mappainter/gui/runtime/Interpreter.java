package com.github.nekit508.mappainter.gui.runtime;

import arc.func.Prov;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Disposable;
import arc.util.Pack;
import com.github.nekit508.mappainter.gui.InterpreterContext;
import com.github.nekit508.mappainter.gui.compiletime.InterpreterCompiler;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Interpreter implements Disposable {
    public Seq<Object> objectsStack = new Seq<>();
    public ObjectMap<String, Prov<Object>> dynamicProviders = new ObjectMap<>();
    public Seq<ByteArrayInputStream> executableBytecodes = new Seq<>();

    public InterpreterContext context;

    public Interpreter(InterpreterContext context) {
        this.context = context;
    }

    public void pushObject(Object object) {
        objectsStack.add(object);
    }

    public Object popObject() {
        return objectsStack.pop();
    }

    public void start(ByteArrayInputStream executableBytecode) {
        executableBytecodes.add(executableBytecode);
    }

    public void end() {
        executableBytecodes.pop();
    }

    public Field getFieldRecursively(String name, Class<?> clazz) {
        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            if (clazz.getSuperclass() != null)
                return getFieldRecursively(name, clazz.getSuperclass());
            throw new RuntimeException(e);
        }
    }

    public Class<?> unwrap(Class<?> primitiveOrWrapper) {
        if (primitiveOrWrapper.isPrimitive())
            return primitiveOrWrapper;

        if (primitiveOrWrapper == Integer.class)
            return int.class;
        else if (primitiveOrWrapper == Float.class)
            return float.class;
        else if (primitiveOrWrapper == Short.class)
            return short.class;
        else if (primitiveOrWrapper == Long.class)
            return long.class;
        else if (primitiveOrWrapper == Byte.class)
            return byte.class;
        else if (primitiveOrWrapper == Boolean.class)
            return boolean.class;
        else if (primitiveOrWrapper == Double.class)
            return double.class;

        return primitiveOrWrapper;
    }

    public Method getMethodRecursively(String name, Class<?> clazz, Class<?>[] params) {
        try {
            var methods = clazz.getDeclaredMethods();
            for (Method method : methods)
                if (isAppropriateMethod(method, name, params))
                    return method;
            return clazz.getDeclaredMethod(name, params);
        } catch (NoSuchMethodException e) {
            if (clazz.getSuperclass() != null)
                return getMethodRecursively(name, clazz.getSuperclass(), params);
            throw new RuntimeException("Method " + name + " " + new Seq<>(params) + " was not founded in " + clazz + ".");
        }
    }

    public boolean isAppropriateMethod(Method method, String name, Class<?>[] params) {
        if (!method.getName().equals(name))
            return false;

        var types = method.getParameterTypes();
        for (int i = 0; i < types.length; i++) {
            var type = types[i];
            var param = params[i];

            if (!unwrap(type).isAssignableFrom(unwrap(param)))
                return false;
        }

        return true;
    }

    public void setField(Object object, Field field, Object newValue) {
        try {
            field.set(object, newValue);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void executeMethod(Object object, Method method, Object[] params) {
        try {
            method.invoke(object, params);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] get(int n) {
        var out = new byte[n];
        for (int i = 0; i < n; i++)
            out[i] = get();
        return out;
    }

    public byte get() {
        var out = executableBytecodes.peek().read();
        if (out == -1)
            throw new RuntimeException("Illegal bytecode end.");
        return (byte) out;
    }

    public int getInt() {
        var b = get(4);
        return Pack.intBytes(b);
    }

    public float getFloat() {
        return Float.intBitsToFloat(getInt());
    }

    public String getString() {
        var size = getInt();
        return new String(get(size));
    }

    public Object getFromRef() {
        var name = getString();
        if (dynamicProviders.containsKey(name))
            return dynamicProviders.get(name).get();
        throw new RuntimeException("Variable " + name + " not declared in this scope.");
    }

    public InterpreterExecutable getExecutable(String name) {
        if (context.executables.containsKey(name))
            return context.getExecutableByName(name);
        else
            throw new RuntimeException("Function " + name + " not declared in this scope.");
    }

    public void setField() {
        var name = getString();
        var value = value();

        var obj = objectsStack.peek();
        setField(obj, getFieldRecursively(name, obj.getClass()), value);
    }

    public Object numeric() {
        Object out;

        var type = get();

        if (type == InterpreterCompiler.intN)
            out = getInt();
        else if (type == InterpreterCompiler.floatN)
            out = getFloat();
        else
            throw new IllegalArgumentException("Wrong numeric type " + type + ".");

        return out;
    }

    public void defaults() {
        var size = getInt();

        var curObj = objectsStack.peek();

        if (curObj instanceof Table t) {
            pushObject(t.defaults());
            for (int i = 0; i < size; i++) {
                var start = get();
                if (start != InterpreterCompiler.methodExecution)
                    throw new IllegalArgumentException("Not method exec start in default settings block.");
                methodExec();
            }
            popObject();
        } else {
            throw new IllegalArgumentException("Defaults settings can be applied only to tables.");
        }
    }

    public boolean booleanV() {
        return get() == 1;
    }

    public Object value() {
        var type = get();

        if (type == InterpreterCompiler.numericValueType) {
            return numeric();
        } else if (type == InterpreterCompiler.stringValueType) {
            return getString();
        } else if (type == InterpreterCompiler.dynamicValueType) {
            return getFromRef();
        } else if (type == InterpreterCompiler.booleanValueType) {
            return booleanV();
        }

        throw new IllegalArgumentException("Unknown value type.");
    }

    public void funcExec() {
        var name = getString();
        var executable = getExecutable(name);
        executable.execute();
    }

    public void methodExec() {
        var name = getString();
        var n = getInt();

        Object[] params = new Object[n];
        Class<?>[] classes = new Class<?>[n];

        for (int i = 0; i < n; i++) {
            params[i] = value();
            classes[i] = params[i].getClass();
        }

        var obj = objectsStack.peek();

        executeMethod(obj, getMethodRecursively(name, obj.getClass(), classes), params);
    }

    public void elementDecl() {
        var method = getString();

        var argsSize = getInt();
        var args = new Object[argsSize];
        for (int i = 0; i < argsSize; i++) {
            args[i] = value();
        }

        var top = objectsStack.peek();
        if (top instanceof Table) {
            var cell = StdElements.elementDecl(method, (Table) objectsStack.peek(), null, args);

            objectsStack.add(cell.get());
            var bodySize = getInt();
            for (int i = 0; i < bodySize; i++)
                statement();
            objectsStack.pop();

            objectsStack.add(cell);
            var cellMembersSize = getInt();
            for (int i = 0; i < cellMembersSize; i++) {
                get();
                methodExec();
            }
            objectsStack.pop();
        } else
            throw new IllegalArgumentException("Elements can be declared only on tables.");
    }

    public void statement() {
        var start = get();

        if (start == InterpreterCompiler.setField) {
            setField();
        } else if (start == InterpreterCompiler.funcExecution) {
            funcExec();
        } else if (start == InterpreterCompiler.methodExecution) {
            methodExec();
        } else if (start == InterpreterCompiler.defaultsSettings) {
            defaults();
        } else if (start == InterpreterCompiler.elementDecl) {
            elementDecl();
        } else {
            throw new IllegalArgumentException("Wrong instruction start byte. " + start + ".");
        }
    }

    public void execute(ByteArrayInputStream executableBytecode) {
        start(executableBytecode);

        var n = getInt();
        for (int i = 0; i < n; i++)
            statement();

        end();
    }

    @Override
    public void dispose() {
        objectsStack.clear();
        dynamicProviders.clear();

        executableBytecodes.clear();
    }
}
