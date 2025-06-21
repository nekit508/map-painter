package com.github.nekit508.annotations.utils;

import arc.func.Cons;
import arc.util.Nullable;
import com.github.nekit508.annotations.BaseProcessor;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.parser.JavacParser;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

public class Utils {
    public BaseProcessor owner;

    public Utils(BaseProcessor owner) {
        this.owner = owner;
    }

    // region raw code parsing
    public List<JCTree> parseMethodDecl(String decl) {
        try {
            var parser = parse(decl);
            var m = JavacParser.class.getDeclaredMethod("classOrInterfaceOrRecordBodyDeclaration", Name.class, boolean.class, boolean.class);
            m.setAccessible(true);
            return (List<JCTree>) m.invoke(parser, name("idk"), false, false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Parses raw code into statement. */
    public JCTree.JCStatement parseStatement(String statement) {

        return parse(statement).parseStatement();
    }

    /** Parses raw code into expression. */
    public JCTree.JCExpression parseExpression(String expression) {
        return parse(expression).parseExpression();
    }

    /** Parses raw code into statements list. */
    public List<JCTree.JCStatement> parseCodeAsList(String codeBlock) {
        return parse("{" + codeBlock + "}").block().stats;
    }

    /** Create parser of raw code. */
    public JavacParser parse(String code) {
        return owner.parserFactory.newParser(code, true, true, true);
    }
    // endregion raw code parsing

    //region structs
    @SafeVarargs
    public final <T> List<T> listOf(T... objects) {
        var out = new ListBuffer<T>();
        out.addAll(Arrays.asList(objects));
        return out.toList();
    }

    @SafeVarargs
    public final <T> List<T> listOf(Collection<T>... objects) {
        ListBuffer<T> buf = new ListBuffer<>();
        for (Collection<T> object : objects)
            buf.addAll(object);
        return buf.toList();
    }

    public <T> List<T> listNil() {
        return List.nil();
    }

    public <T> List<T> listModified(List<T> list, Cons<LinkedList<T>> modifier) {
        var buffer = new LinkedList<>(list);
        modifier.get(buffer);
        return listOf(buffer);
    }

    @SafeVarargs
    public final <T> List<T> listExtended(List<T> list, T... objects) {
        var buffer = new LinkedList<>(list);
        buffer.addAll(Arrays.asList(objects));
        return listOf(buffer);
    }
    //endregion

    //region debug print
    public void printNote(Object... objects) {
        owner.messager.printNote(toString(objects));
    }

    public void printNote(Element element, Object... objects) {
        owner.messager.printNote(toString(objects), element);
    }

    public void printError(Object... objects) {
        owner.messager.printError(toString(objects));
    }

    public void printError(Element element, Object... objects) {
        owner.messager.printError(toString(objects), element);
    }

    public void printWarning(Object... objects) {
        owner.messager.printWarning(toString(objects));
    }

    public void printWarning(Element element, Object... objects) {
        owner.messager.printWarning(toString(objects), element);
    }

    /** Returns string with all objects string representations separated with space. */
    public String toString(Object... objects) {
        String[] t = new String[objects.length];
        for (int i = 0; i < objects.length; i++)
            t[i] = objects[i].toString();
        return String.join(" ", t);
    }
    //endregion

    //region symbols
    public Symbol.MethodSymbol methodSymbol(long modifiers, Name name, Type returnType, Symbol owner) {
        return new Symbol.MethodSymbol(modifiers, name, returnType, owner);
    }

    public @Nullable Symbol.ClassSymbol classSymbol(Class<?> clazz) {
        return owner.symtab.getClassesForName(name(clazz.getCanonicalName())).iterator().next();
    }

    public Symbol.MethodSymbol lookupMethodSymbol(Symbol.ClassSymbol owner, Name methodName, Type returnType, List<Type> args) {
        var enclosed = owner.getEnclosedElements();
        f: for (Symbol symbol : enclosed) {
            if (symbol instanceof Symbol.MethodSymbol) {
                var methodSymbol = (Symbol.MethodSymbol) symbol;

                var type = (Type.MethodType) methodSymbol.asType();
                //Log.info("Method name: @", methodSymbol.name);
                if (!methodSymbol.name.equals(methodName)) {
                    //Log.info("EName: @ != @", methodSymbol.name, methodName);
                    continue;
                }

                if (!type.getReturnType().equals(returnType)) {
                    //Log.info("EReturn: @ != @", type.getReturnType(), returnType);
                    continue;
                }

                var params = type.getParameterTypes();
                if (params.size() != args.size()) {
                    //Log.info("ESize: @ != @", params.size(), args.size());
                    continue;
                }
                for (int i = 0; i < params.size(); i++)
                    if (!params.get(i).equals(args.get(i))) {
                        //Log.info("EParams: @ != @", params.get(i), args.get(i));
                        continue f;
                    }

                //Log.info("Founded");
                return methodSymbol;
            }
        }
        throw new IllegalArgumentException("Method name:(" + methodName + ") returnType:(" + returnType + ") params:(" + args +  ") not founded in " + owner + ".");
    }
    //endregion symbols

    //region tree building
    public JCTree.JCReturn returnn(JCTree.JCExpression expression) {
        return owner.treeMaker.Return(expression);
    }

    public JCTree.JCExpression executeMethodOnObject(JCTree.JCExpression obj, Symbol.MethodSymbol method, List<JCTree.JCExpression> args) {
        return owner.treeMaker.App(owner.treeMaker.Select(obj, method), args);
    }

    public Utils pos(int pos) {
        owner.treeMaker.at(pos);
        return this;
    }

    public JCTree.JCExpressionStatement exec(JCTree.JCExpression expr) {
        return owner.treeMaker.Exec(expr);
    }

    public JCTree.JCMethodDecl method(Symbol.MethodSymbol methodSymbol, JCTree.JCBlock body) {
        return owner.treeMaker.MethodDef(methodSymbol, body);
    }

    public JCTree.JCPrimitiveTypeTree primitive(TypeTag type) {
        return owner.treeMaker.TypeIdent(type);
    }

    public JCTree.JCVariableDecl localVar(JCTree.JCExpression type, String name, JCTree.JCExpression init) {
        return owner.treeMaker.VarDef(modifiers(0), name(name), type, init);
    }

    public JCTree.JCVariableDecl localVar(JCTree.JCExpression type, String name) {
        return localVar(type, name, null);
    }

    public JCTree.JCVariableDecl paramVar(JCTree.JCExpression type, String name, Symbol paramOwner) {
        return owner.treeMaker.Param(name(name), type.type, paramOwner);
    }

    public JCTree.JCExpression assign(JCTree.JCExpression var, JCTree.JCExpression value) {
        return owner.treeMaker.Assign(var, value).setType(var.type);
    }

    public JCTree.JCExpression cast(JCTree.JCExpression type, JCTree.JCExpression value) {
        return owner.treeMaker.TypeCast(type, value).setType(type.type);
    }
    
    public JCTree.JCBlock codeBlock(Collection<JCTree.JCStatement> statements) {
        return owner.treeMaker.Block(0,
                statements instanceof List<JCTree.JCStatement> ?
                        (List<JCTree.JCStatement>) statements :
                        listOf(statements)
        );
    }

    public JCTree.JCStatement call(JCTree.JCExpression expression) {
        return owner.treeMaker.Call(expression);
    }

    public JCTree.JCExpression select(String path) {
        if (!path.contains("."))
            return ident(name(path));
        var ind = path.indexOf('.');
        return select(ident(name(path.substring(0, ind))), path.substring(ind + 1));
    }

    public JCTree.JCExpression select(JCTree.JCExpression from, String path) {
        var parts = path.split("\\.");
        for (String part : parts)
            from = select(from, name(part));
        return from;
    }

    public JCTree.JCExpression select(JCTree.JCExpression from, Name path) {
        return owner.treeMaker.Select(from, path);
    }

    public JCTree.JCExpression select(JCTree.JCExpression from, Symbol path) {
        return owner.treeMaker.Select(from, path);
    }

    public JCTree.JCExpression executeMethod(JCTree.JCExpression method,
                                             List<JCTree.JCExpression> typeargs,
                                             List<JCTree.JCExpression> args) {
        return owner.treeMaker.Apply(typeargs, method, args).setType(method.type.getReturnType());
    }

    public JCTree.JCExpression executeMethodOnObject(JCTree.JCExpression object, String methodName,
                                                     List<JCTree.JCExpression> typeargs,
                                                     List<JCTree.JCExpression> args, Type returnType) {
        return executeMethod(select(object, methodName).setType(returnType), typeargs, args);
    }

    public JCTree.JCExpression ident(JCTree.JCVariableDecl var) {
        return owner.treeMaker.Ident(var);
    }

    public JCTree.JCExpression ident(Name name) {
        return owner.treeMaker.Ident(name);
    }

    public JCTree.JCExpression ident(Symbol symbol) {
        return owner.treeMaker.QualIdent(symbol);
    }

    public Name name(String name) {
        return owner.names.fromString(name);
    }

    public JCTree.JCModifiers modifiers(long flags, @Nullable JCTree.JCAnnotation... annotations) {
        return annotations == null ? owner.treeMaker.Modifiers(flags) : owner.treeMaker.Modifiers(flags, listOf(annotations));
    }

    public JCTree.JCMethodDecl getTree(ExecutableElement element) {
        return (JCTree.JCMethodDecl) owner.trees.getTree(element);
    }

    public JCTree.JCVariableDecl getTree(VariableElement element) {
        return (JCTree.JCVariableDecl) owner.trees.getTree(element);
    }

    public JCTree.JCClassDecl getTree(TypeElement element) {
        return (JCTree.JCClassDecl) owner.trees.getTree(element);
    }
    //endregion

    //region typing
    public Type unboxedTypeMirror(Type type) {
        try {
            return owner.javacTypes.unboxedTypeOrType(type);
        } catch (IllegalArgumentException ignored) {}
        return type;
    }

    public Type getRawDeclaredTypeMirror(DeclaredType type) {
        return (Type) type.asElement().asType();
    }

    public Type getTypeMirror(CharSequence name) {
        return owner.elements.getTypeElement(name).asType();
    }

    public Type getTypeMirror(Class<?> clazz) {
        return getTypeMirror(clazz.getCanonicalName());
    }
    //endregion

    //region annotations
    public <T extends Annotation> boolean hasAnno(AnnotatedConstruct element, Class<T> anno) {
        return element.getAnnotation(anno) != null;
    }

    public <T extends Annotation> Annotation getAnno(AnnotatedConstruct element, Class<T> anno) {
        return element.getAnnotation(anno);
    }
    //endregion
}
