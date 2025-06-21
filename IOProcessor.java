package com.github.nekit508.annotations.ioproc;

import arc.util.io.Reads;
import arc.util.io.Writes;
import com.github.nekit508.annotations.BaseProcessor;
import com.github.nekit508.annotations.NSupportedAnnotationTypes;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.ListBuffer;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// @AnnotationProcessor
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@NSupportedAnnotationTypes({
        IOAnnotations.Gen.class,
        IOAnnotations.Provider.class
})
@SupportedOptions({
        "com.github.nekit508.annotations.ioproc.IOProc.logIOMethods"
})
public class IOProcessor extends BaseProcessor {
    protected long tempVarId = 0;

    public HashMap<TypeMirror, IOMethodProvider> ioProviders = new LinkedHashMap<>();

    public boolean logIOMethods = false;

    public IOProcessorEnv ioProcessorEnv;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        for (TypeKind kind : TypeKind.values())
            if (kind.isPrimitive())
                ioProviders.put(types.getPrimitiveType(kind), new PrimitiveIOMethodProvider(kind));

        var options = processingEnv.getOptions();
        logIOMethods = Boolean.parseBoolean(options.getOrDefault("com.github.nekit508.annotations.ioproc.IOProc.logIOMethods", "false"));

        ioProcessorEnv = new IOProcessorEnv(this);;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        super.process(annotations, roundEnv);
        processProviders();
        processIO();
        return false;
    }

    public void processProviders() {
        var providers = roundEnv.getElementsAnnotatedWith(IOAnnotations.Provider.class);

        if (providers.isEmpty()) return;

        var providerMethodsCandidates = providers.stream().flatMap(e ->
                e instanceof TypeElement ?
                        e.getEnclosedElements().stream().filter(f ->
                                f instanceof ExecutableElement && f.getAnnotation(IOAnnotations.Provider.class) == null
                        ).map(f -> (ExecutableElement) f) :
                        e instanceof ExecutableElement ?
                                Stream.of((ExecutableElement) e) :
                                Stream.empty()
        ).filter(e -> e.getKind() == ElementKind.METHOD && e.getAnnotation(IOAnnotations.Ignore.class) == null && e.getModifiers().contains(Modifier.STATIC)).collect(Collectors.toSet());

        // [write, read, readCreate]
        var candidatesMap = new LinkedHashMap<TypeMirror, ExecutableElement[]>();
        for (ExecutableElement candidate : providerMethodsCandidates) {
            var returnType = candidate.getReturnType();
            var params = candidate.getParameters();
            var typeParams = candidate.getTypeParameters();
            boolean create = true;
            boolean read;

            if (returnType.getKind() != TypeKind.VOID) {
                if (params.size() != 1 && params.size() != 2)
                    utils.printError(candidate, "IO reader must accept Reads as first parameter and optionally returnType as second");

                var streamType = params.get(0).asType();
                if (!types.isSubtype(elements.getTypeElement(Reads.class.getCanonicalName()).asType(), streamType))
                    utils.printError(candidate, "IO reader must accept Reads as first parameter.");

                if (params.size() == 2) {
                    var type = params.get(1).asType();
                    if (!types.isSameType(returnType, type))
                        utils.printError(candidate, "IO reader's second parameter must be same as returnType");
                    create = false;
                }

                read = true;
            } else {
                if (params.size() != 2)
                    utils.printError(candidate, "IO writer must accept Writes as first parameter and <? extends Object> as second");

                if (!types.isSubtype(elements.getTypeElement(Writes.class.getCanonicalName()).asType(), params.get(0).asType()))
                    utils.printError(candidate, "IO write must accept Writes as first parameter");

                read = false;
            }

            var ioType = utils.getRawDeclaredTypeMirror((DeclaredType) (read ? returnType : params.get(1).asType()));

            if (!candidatesMap.containsKey(ioType))
                candidatesMap.put(ioType, new ExecutableElement[3]);

            var mas = candidatesMap.get(ioType);
            var ind = !read
                    ? 0
                    : !create ? 1 : 2;
            if (mas[ind] != null)
                utils.printWarning("Multiple declaration of io method with signature " + returnType + " " + candidate);

            mas[ind] = candidate;
        }

        for (TypeMirror type : candidatesMap.keySet()) {
            var mas = candidatesMap.get(type);

            if (ioProviders.containsKey(type))
                utils.printWarning("Redeclaration of io methods for type " + type);

            ioProviders.put(type, new CustomIOMethodProvider(
                    mas[0] == null ? null : utils.select(utils.ident(utils.getTree((TypeElement) mas[0].getEnclosingElement()).name), utils.getTree(mas[0]).name),
                    mas[1] == null ? null : utils.select(utils.ident(utils.getTree((TypeElement) mas[1].getEnclosingElement()).name), utils.getTree(mas[1]).name),
                    mas[2] == null ? null : utils.select(utils.ident(utils.getTree((TypeElement) mas[2].getEnclosingElement()).name), utils.getTree(mas[2]).name)
            ));
        }

        var keys = ioProviders.keySet();
        for (TypeMirror type : keys) {
            var provider = ioProviders.get(type);
            if (provider instanceof CustomIOMethodProvider) {
                if (((CustomIOMethodProvider) provider).read == null)
                    utils.printWarning("read method does not set for " + type);
                if (((CustomIOMethodProvider) provider).write == null)
                    utils.printWarning("write method does not set for " + type);
                if (((CustomIOMethodProvider) provider).readCreate == null)
                    utils.printWarning("readCreate method does not set for " + type);
            }
        }

        if (logIOMethods) {
            utils.printNote("IO providers:");
            for (TypeMirror type : keys) {
                var provider = ioProviders.get(type);
                utils.printNote("    " + type);
                if (provider instanceof CustomIOMethodProvider) {
                    var typeProvider = (CustomIOMethodProvider) provider;
                    utils.printNote("    |   write: " + (typeProvider.write == null ? "null" : typeProvider.write));
                    utils.printNote("    |   read: " + (typeProvider.read == null ? "null" : typeProvider.read));
                    utils.printNote("    |   readCreate: " + (typeProvider.readCreate == null ? "null" : typeProvider.readCreate));
                }
            }
            utils.printNote("IO providers end.");
        }
    }

    public void processIO() {
        Map<TypeElement, Set<VariableElement>> map = new LinkedHashMap<>();

        var elements = roundEnv.getElementsAnnotatedWith(IOAnnotations.Gen.class).stream()
                .filter(e -> e.getAnnotation(IOAnnotations.Ignore.class) == null)
                .toList();

        for (Element element : elements) {
            if (element instanceof TypeElement) {
                var t = (TypeElement) element;

                var set = new LinkedHashSet<VariableElement>();
                map.put(t, set);

                set.addAll(t.getEnclosedElements().stream()
                        .filter(e -> e.getAnnotation(IOAnnotations.Ignore.class) == null && e instanceof VariableElement)
                        .map(e -> (VariableElement) e)
                        .toList());
            } else if (element instanceof VariableElement) {
                var v = (VariableElement) element;
                var t = (TypeElement) v.getEnclosingElement();

                if (!map.containsKey(t))
                    map.put(t, new LinkedHashSet<>());

                var set = map.get(t);
                set.add(v);
            }
        }

        for (TypeElement type : map.keySet()) {
            var fields = map.get(type);
            var typeTree = utils.getTree(type);
            var genObject = type.getAnnotation(IOAnnotations.GenObject.class);

            typeTree.defs.forEach(e -> {
                if (e instanceof JCTree.JCMethodDecl &&
                        ((JCTree.JCMethodDecl) e).name.toString().equals("read") &&
                        ((JCTree.JCMethodDecl) e).sym.getParameters().size() == 1 &&
                        ((JCTree.JCMethodDecl) e).sym.getParameters().get(0).type.equals(utils.getTypeMirror(Reads.class)))
                    utils.printError(((JCTree.JCMethodDecl) e).sym, "io dummy methods must not be defined");
                else if (e instanceof JCTree.JCMethodDecl &&
                        ((JCTree.JCMethodDecl) e).name.toString().equals("write") &&
                        ((JCTree.JCMethodDecl) e).sym.getParameters().size() == 1 &&
                        ((JCTree.JCMethodDecl) e).sym.getParameters().get(0).type.equals(utils.getTypeMirror(Writes.class)))
                    utils.printError(((JCTree.JCMethodDecl) e).sym, "io dummy methods must not be defined");
            });


            typeTree.defs = utils.listModified(typeTree.defs, l -> {
                LinkedList<JCTree.JCStatement> readBlocks = new LinkedList<>(), writeBlocks = new LinkedList<>();

                for (VariableElement field : fields) {
                    var gen = field.getAnnotation(IOAnnotations.Gen.class);

                    if (gen.autoCreation())
                        messager.printError("AutoCreation not implemented yet", field);

                    var provider = getIOMethodProvider(field.asType());

                    readBlocks.add(
                            provider.getMethod(this, utils.ident(utils.name("read")), utils.ident(utils.getTree(field)), field.asType(), true, gen.autoCreation())
                    );
                    writeBlocks.add(
                            provider.getMethod(this, utils.ident(utils.name("read")), utils.ident(utils.getTree(field)), field.asType(), false, gen.autoCreation())
                    );
                }

                var readMethod = utils.method(
                        utils.methodSymbol(Flags.PUBLIC, utils.name("read"), new Type.JCVoidType(), typeTree.sym),
                        utils.codeBlock(writeBlocks)
                );
                readMethod.params = utils.listExtended(readMethod.params, utils.paramVar(utils.ident(utils.classSymbol(Reads.class)), "reads"));
                var writeMethod = utils.method(
                        utils.methodSymbol(Flags.PUBLIC, utils.name("write"), new Type.JCVoidType(), typeTree.sym),
                        utils.codeBlock(writeBlocks)
                );
                writeMethod.params = utils.listExtended(writeMethod.params, utils.paramVar(utils.ident(utils.classSymbol(Writes.class)), "writes"));
            });

            typeTree.implementing = utils.listModified(typeTree.implementing, l ->
                    l.removeIf(e ->
                            ((JCTree.JCIdent) e).sym.type.equals(this.elements.getTypeElement("com.github.nekit508.annotations.ioproc.IO").asType())
                    )
            );
        }
    }

    public IOMethodProvider getIOMethodProvider(TypeMirror type) {
        return ioProviders.get(
                utils.unboxedTypeMirror(type instanceof DeclaredType ? utils.getRawDeclaredTypeMirror((DeclaredType) type) : type)
        );
    }

    public JCTree.JCStatement[][] constructTypeIO(JCTree.JCVariableDecl reads, JCTree.JCVariableDecl writes, JCTree.JCExpression variable, TypeMirror expressionType) {
        expressionType = utils.unboxedTypeMirror(expressionType);

        if (expressionType instanceof PrimitiveType) {
            return null;
        } else if (expressionType instanceof DeclaredType) {
            var fieldRawType = utils.getRawDeclaredTypeMirror((DeclaredType) expressionType);

            List<JCTree.JCStatement> readStatements = new LinkedList<>(), writeStatements = new LinkedList<>();

            if (ioProviders.containsKey(fieldRawType)) {

            } if (types.isSubtype(utils.getTypeMirror("arc.struct.Seq"), fieldRawType)) {
                var boxedType = ((DeclaredType) expressionType).getTypeArguments().get(0);
                List<JCTree.JCStatement> writeSubIO = new LinkedList<>(), readSubIO = new LinkedList<>();
                String ioVarName = nextVar(), readLoopCounter = nextVar();
                var ioVarDecl = treeMaker.VarDef(utils.modifiers(0), utils.name(ioVarName), utils.ident(utils.name(types.asElement(boxedType).getSimpleName().toString())), null);

                var d = constructTypeIO(reads, writes, utils.ident(utils.name(ioVarName)), boxedType);
                readSubIO.add(ioVarDecl);
                saveTypeIOOutput(readSubIO, writeSubIO, d);
                readSubIO.add(treeMaker.Exec(utils.executeMethodOnObject(variable, "add", utils.listNil(), utils.listOf(
                        utils.ident(utils.name(ioVarName))
                ))));

                writeStatements.add(primitiveWriteValue(writes, utils.select(variable, "size"), "i"));
                writeStatements.add(treeMaker.ForeachLoop(
                        ioVarDecl,
                        variable,
                        treeMaker.Block(0, utils.listOf(writeSubIO))
                ));

                String sizeVarName = "size" + tempVarId++;
                readStatements.add(treeMaker.VarDef(
                        utils.modifiers(0), utils.name(sizeVarName), utils.primitive(TypeTag.INT),
                        utils.executeMethodOnObject(utils.ident(reads), "i", utils.listNil(), utils.listNil())
                ));
                readStatements.add(treeMaker.ForLoop(
                        utils.listOf(treeMaker.VarDef(
                                utils.modifiers(0), utils.name(readLoopCounter), utils.primitive(TypeTag.INT), treeMaker.Literal(0)
                        )),
                        treeMaker.Binary(JCTree.Tag.LT, utils.ident(utils.name(readLoopCounter)), utils.select(sizeVarName)),
                        utils.listOf(treeMaker.Exec(treeMaker.Unary(JCTree.Tag.POSTINC, utils.ident(utils.name(readLoopCounter))))),
                        treeMaker.Block(0, utils.listOf(readSubIO))
                ));
            }

            return new JCTree.JCStatement[][]{
                    readStatements.toArray(new JCTree.JCStatement[0]),
                    writeStatements.toArray(new JCTree.JCStatement[0])
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

    public String nextVar() {
        return "var" + tempVarId++;
    }

    public JCTree.JCStatement primitiveReadInVar(JCTree.JCVariableDecl reads, JCTree.JCExpression field, String methodName) {
        return treeMaker.Exec(treeMaker.Assign(field, utils.executeMethodOnObject(utils.ident(reads), methodName, utils.listNil(), utils.listNil())));
    }

    public JCTree.JCStatement primitiveWriteValue(JCTree.JCVariableDecl writes, JCTree.JCExpression value, String methodName) {
        return treeMaker.Exec(utils.executeMethodOnObject(utils.ident(writes), methodName, utils.listNil(), utils.listOf(value)));
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
}
