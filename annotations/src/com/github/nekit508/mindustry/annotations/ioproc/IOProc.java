package com.github.nekit508.mindustry.annotations.ioproc;

import com.sun.source.tree.Tree;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.parser.JavacParser;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

// @AnnotationProcessor
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({"com.github.nekit508.mindustry.annotations.ioproc.IOProc.IOField"})
public class IOProc extends AbstractProcessor {
    public Types types;
    public Elements elements;
    public Filer filer;
    public Messager messager;

    public Trees trees;
    public TreeMaker treeMaker;
    public Names names;
    public com.sun.tools.javac.code.Types treeTypes;
    public Context context;
    public ParserFactory parserFactory;

    protected long tempVarId = 0;

    public HashMap<TypeMirror, Object> ioMethods = new HashMap<>();

    public HashMap<TypeKind, String> primitiveIOMethods = new HashMap<>(){{
        put(TypeKind.FLOAT, "f");
        put(TypeKind.INT, "i");
        put(TypeKind.BOOLEAN, "bool");
        put(TypeKind.BYTE, "b");
        put(TypeKind.DOUBLE, "d");
        put(TypeKind.LONG, "l");
        put(TypeKind.SHORT, "s");
        //put(TypeKind.CHAR, "s");
    }};

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        HashMap<TypeElement, List<VariableElement>> fieldsMap = new HashMap<>();

        var providers = roundEnv.getElementsAnnotatedWith(IOProvider.class);


        var e = roundEnv.getElementsAnnotatedWith(IOField.class);
        for (Element element : e) {
            if (element instanceof VariableElement) {
                var field = (VariableElement) element;
                TypeElement type = (TypeElement) field.getEnclosingElement();

                if (!fieldsMap.containsKey(type))
                    fieldsMap.put(type, new LinkedList<>());

                fieldsMap.get(type).add(field);
            }
        }

        List<JCTree.JCStatement> readStatements = new LinkedList<>(), writeStatements = new LinkedList<>();

        fieldsMap.forEach((type, fields) -> {
            readStatements.clear();
            writeStatements.clear();

            JCTree.JCMethodDecl read = null, write = null;

            for (Element element : type.getEnclosedElements()) {
                if (element instanceof ExecutableElement) {
                    var method = (ExecutableElement) element;
                    if (checkMethodSignature(method, elements.getName("write"), null, new ArrayList<>(){{
                        add(elements.getTypeElement("arc.util.io.Writes").asType());
                    }})) {
                        messager.printNote("Found write " + method);
                        write = (JCTree.JCMethodDecl) trees.getTree(method);
                    } else if (checkMethodSignature(method, elements.getName("read"), null, new ArrayList<>(){{
                        add(elements.getTypeElement("arc.util.io.Reads").asType());
                    }})) {
                        messager.printNote("Found read " + method);
                        read = (JCTree.JCMethodDecl) trees.getTree(method);
                    }
                }
            }

            if (write == null)
                throw new RuntimeException("write method was not found in " + type);
            if (read == null)
                throw new RuntimeException("read method was not found in " + type);

            for (VariableElement field : fields) {
                var d = constructTypeIO(read.getParameters().get(0), write.getParameters().get(0), ident((JCTree.JCVariableDecl) getTree(field)), field.asType());
                saveTypeIOOutput(readStatements, writeStatements, d);
            }

            addElementsToTree(
                    read,
                    readStatements,
                    t -> t.getBody().stats,
                    (t, s) -> t.getBody().stats = s.toList()
            );

            addElementsToTree(
                    write,
                    writeStatements,
                    t -> t.getBody().stats,
                    (t, s) -> t.getBody().stats = s.toList()
            );

            messager.printNote(write.toString());
            messager.printNote(read.toString());
        });

        return false;
    }

    public JCTree.JCStatement[][] constructTypeIO(JCTree.JCVariableDecl reads, JCTree.JCVariableDecl writes, JCTree.JCExpression variable, TypeMirror expressionType) {
        expressionType = unboxed(expressionType);

        if (expressionType instanceof PrimitiveType) {
            return constructPrimitiveTypeIO(reads, writes, variable, expressionType);
        } else if (expressionType instanceof DeclaredType) {
            var fieldRawType = getRawDeclaredType((DeclaredType) expressionType);

            List<JCTree.JCStatement> readStatements = new LinkedList<>(), writeStatements = new LinkedList<>();

            if (types.isSubtype(getType("arc.struct.Seq"), fieldRawType)) {
                var boxedType = ((DeclaredType) expressionType).getTypeArguments().get(0);
                List<JCTree.JCStatement> writeSubIO = new LinkedList<>(), readSubIO = new LinkedList<>();
                String ioVarName = nextVar(), readLoopCounter = nextVar();
                var ioVarDecl = treeMaker.VarDef(modifiers(0), name(ioVarName), ident(name(types.asElement(boxedType).getSimpleName().toString())), null);

                var d = constructTypeIO(reads, writes, ident(name(ioVarName)), boxedType);
                readSubIO.add(ioVarDecl);
                saveTypeIOOutput(readSubIO, writeSubIO, d);
                readSubIO.add(treeMaker.Exec(executeMethodOnObject(variable, "add", nil(), of(
                        ident(name(ioVarName))
                ))));

                writeStatements.add(primitiveWriteValue(writes, getFromObject(variable, "size"), "i"));
                writeStatements.add(treeMaker.ForeachLoop(
                        ioVarDecl,
                        variable,
                        treeMaker.Block(0, of(writeSubIO))
                ));

                String sizeVarName = "size" + tempVarId++;
                readStatements.add(treeMaker.VarDef(
                        modifiers(0), name(sizeVarName), primitive(TypeTag.INT),
                        executeMethodOnObject(ident(reads), "i", nil(), nil())
                ));
                readStatements.add(treeMaker.ForLoop(
                        of(treeMaker.VarDef(
                                modifiers(0), name(readLoopCounter), primitive(TypeTag.INT), treeMaker.Literal(0)
                        )),
                        treeMaker.Binary(JCTree.Tag.LT, ident(name(readLoopCounter)), getVar(sizeVarName)),
                        of(treeMaker.Exec(treeMaker.Unary(JCTree.Tag.POSTINC, ident(name(readLoopCounter))))),
                        treeMaker.Block(0, of(readSubIO))
                ));
            }

            return new JCTree.JCStatement[][]{
                    readStatements.toArray(new JCTree.JCStatement[0]),
                    writeStatements.toArray(new JCTree.JCStatement[0])
            };
        }

        return null;
    }

    public JCTree.JCStatement[][] constructPrimitiveTypeIO(JCTree.JCVariableDecl reads, JCTree.JCVariableDecl writes, JCTree.JCExpression variable, TypeMirror fieldType) {
        fieldType = unboxed(fieldType);
        if (!primitiveIOMethods.containsKey(fieldType.getKind()))
            messager.printWarning(fieldType.getKind() + " not supported!");
        else {
            return new JCTree.JCStatement[][]{
                    {primitiveReadInVar(reads, variable, primitiveIOMethods.get(fieldType.getKind()))},
                    {primitiveWriteValue(writes, variable, primitiveIOMethods.get(fieldType.getKind()))}
            };
        }
        return null;
    }

    public void saveTypeIOOutput(List<JCTree.JCStatement> readStatements, List<JCTree.JCStatement> writeStatements, JCTree.JCStatement[][] output) {
        if (output != null) {
            readStatements.addAll(Arrays.asList(output[0]));
            writeStatements.addAll(Arrays.asList(output[1]));
        }
    }

    public <T extends JCTree, Y extends JCTree> void addElementsToTree(Y tree, Collection<T> elements, Function<Y, com.sun.tools.javac.util.List<T>> getter, BiConsumer<Y, ListBuffer<T>> setter) {
        var buffer = new ListBuffer<T>();
        buffer.addAll(getter.apply(tree));
        buffer.addAll(elements);
        setter.accept(tree, buffer);
    }

    public JCTree.JCExpression select(String path) {
        if (!path.contains("."))
            path = "java.lang." + path;
        var ind = path.indexOf('.');
        print(path);
        return select(ident(name(path.substring(0, ind))), path.substring(ind + 1));
    }

    public JCTree.JCExpression getVar(String name) {
        return ident(name(name));
    }

    public JCTree.JCPrimitiveTypeTree primitive(TypeTag type) {
        return treeMaker.TypeIdent(type);
    }

    public JCTree.JCVariableDecl newLocalVar(JCTree.JCExpression type, String name) {
        return treeMaker.VarDef(modifiers(0), name(name), type, null);
    }

    public void print(String note) {
        messager.printNote(note);
    }

    public JCTree.JCExpression select(JCTree.JCExpression from, String path) {
        var parts = path.split("\\.");
        for (String part : parts)
            from = treeMaker.Select(from, name(part));
        return from;
    }

    public JCTree.JCExpression getFromObject(JCTree.JCExpression object, String field) {
        return treeMaker.Select(object, name(field));
    }

    public JCTree.JCExpression executeMethod(JCTree.JCExpression method,
                                             com.sun.tools.javac.util.List<JCTree.JCExpression> typeargs,
                                             com.sun.tools.javac.util.List<JCTree.JCExpression> args) {
        return treeMaker.Apply(typeargs, method, args);
    }

    public JCTree.JCExpression executeMethodOnObject(JCTree.JCExpression object, String methodName,
                                                     com.sun.tools.javac.util.List<JCTree.JCExpression> typeargs,
                                                     com.sun.tools.javac.util.List<JCTree.JCExpression> args) {
        return executeMethod(getFromObject(object, methodName), typeargs, args);
    }

    public JCTree.JCStatement primitiveReadInVar(JCTree.JCVariableDecl reads, JCTree.JCExpression field, String methodName) {
        return treeMaker.Exec(treeMaker.Assign(field, executeMethodOnObject(ident(reads), methodName, nil(), nil())));
    }

    public JCTree.JCStatement primitiveWriteValue(JCTree.JCVariableDecl writes, JCTree.JCExpression value, String methodName) {
        return treeMaker.Exec(executeMethodOnObject(ident(writes), methodName, nil(), of(value)));
    }

    public JCTree.JCExpression ident(JCTree.JCVariableDecl var) {
        return treeMaker.Ident(var);
    }

    public JCTree.JCExpression ident(com.sun.tools.javac.util.Name name) {
        return treeMaker.Ident(name);
    }

    public TypeMirror unboxed(TypeMirror type) {
        try {
            return types.unboxedType(type);
        } catch (IllegalArgumentException ignored) {}
        return type;
    }

    public TypeMirror getRawDeclaredType(DeclaredType type) {
        return types.asElement(type).asType();
    }

    public TypeMirror getType(CharSequence name) {
        return elements.getTypeElement(name).asType();
    }

    public <T> com.sun.tools.javac.util.List<T> nil() {
        return com.sun.tools.javac.util.List.nil();
    }
    
    public com.sun.tools.javac.util.Name name(String name) {
        return names.fromString(name);
    }

    public JCTree.JCModifiers modifiers(long flags) {
        return treeMaker.Modifiers(flags);
    }

    public String nextVar() {
        return "var" + tempVarId++;
    }

    @SafeVarargs
    public final <T> com.sun.tools.javac.util.List<T> of(T... objects) {
        var out = new ListBuffer<T>();
        out.addAll(Arrays.asList(objects));
        return out.toList();
    }

    @SafeVarargs
    public final <T> com.sun.tools.javac.util.List<T> of(Collection<T>... objects) {
        ListBuffer<T> buf = new ListBuffer<>();
        for (Collection<T> object : objects)
            buf.addAll(object);
        return buf.toList();
    }

    public <T extends Element, Y extends Tree> Y getTree(T element) {
        return (Y) trees.getTree(element);
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        types = processingEnv.getTypeUtils();
        elements = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();

        context = ((JavacProcessingEnvironment) processingEnv).getContext();
        trees = Trees.instance(processingEnv);
        treeMaker = TreeMaker.instance(context);
        names = Names.instance(context);
        treeTypes = com.sun.tools.javac.code.Types.instance(context);
        parserFactory = ParserFactory.instance(context);
    }

    public JCTree.JCStatement parseStatement(String statement) {
        return parse(statement).parseStatement();
    }

    public JCTree.JCExpression parseExpression(String expression) {
        return parse(expression).parseExpression();
    }

    public com.sun.tools.javac.util.List<JCTree.JCStatement> parseCodeAsList(String codeBlock) {
        return parse("{" + codeBlock + "}").block().stats;
    }

    public JavacParser parse(String code) {
        return parserFactory.newParser(code, true, true, true);
    }

    public boolean checkMethodSignature(ExecutableElement element, Name name, TypeMirror returnType, List<TypeMirror> parameters) {
        if (element.getSimpleName().equals(name)) {
            if ((returnType == null && element.getReturnType().getKind() == TypeKind.VOID) || element.getReturnType().equals(returnType)) {
                var elementParameters = element.getParameters();
                if (elementParameters.size() == parameters.size()) {
                    for (int i = 0; i < elementParameters.size(); i++) {
                        if (elementParameters.get(i).equals(parameters.get(i)))
                            return false;
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface IOField {
        boolean autoCreation() default false;
    }

    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface IOProvider {
    }
}
