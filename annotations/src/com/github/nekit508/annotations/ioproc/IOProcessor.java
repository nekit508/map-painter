package com.github.nekit508.annotations.ioproc;

import arc.util.Log;
import arc.util.io.Reads;
import arc.util.io.Writes;
import com.github.nekit508.annotations.BaseProcessor;
import com.github.nekit508.annotations.NSupportedAnnotationTypes;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// @AnnotationProcessor
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@NSupportedAnnotationTypes({
        IOAnnotations.GenObject.class,
        IOAnnotations.GenObject.Gen.class,
        IOAnnotations.Provider.class
})
@SupportedOptions({
        "com.github.nekit508.annotations.ioproc.IOProc.logIOMethods"
})
public class IOProcessor extends BaseProcessor {
    public HashMap<TypeMirror, IOMethodProvider> ioProviders = new LinkedHashMap<>();
    public HashMap<JCTree.JCMethodDecl, Integer> uniProvidersTypes = new LinkedHashMap<>();

    public boolean logIOMethods = false;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        for (TypeKind kind : TypeKind.values())
            if (kind.isPrimitive())
                ioProviders.put(types.getPrimitiveType(kind), new PrimitiveIOMethodProvider(kind));

        var options = processingEnv.getOptions();
        logIOMethods = Boolean.parseBoolean(options.getOrDefault("com.github.nekit508.annotations.ioproc.IOProc.logIOMethods", "false"));
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        super.process(annotations, roundEnv);
        processIOProviders();
        // TODO fsdafasdf
        //processIOUniProviders();
        processIOGen();
        return false;
    }

    /**
     * 0 - write <br>
     * 1 - read <br>
     * 2 - readCreate <br>
     */
    public int processIOProviderType(Symbol.MethodSymbol symbol) {
        var returnType = symbol.getReturnType();
        var params = symbol.getParameters();
        var typeParams = symbol.getTypeParameters();
        boolean create = true;
        boolean read;

        if (returnType.getKind() != TypeKind.VOID) {
            if (params.size() != 1 && params.size() != 2)
                utils.printError(symbol, "IO reader must accept Reads as first parameter and optionally returnType as second");

            var streamType = params.get(0).asType();
            if (!types.isSubtype(elements.getTypeElement(Reads.class.getCanonicalName()).asType(), streamType))
                utils.printError(symbol, "IO reader must accept Reads as first parameter.");

            if (params.size() == 2) {
                var type = params.get(1).asType();
                if (!types.isSameType(returnType, type))
                    utils.printError(symbol, "IO reader's second parameter must be same as returnType");
                create = false;
            }

            read = true;
        } else {
            if (params.size() != 2)
                utils.printError(symbol, "IO writer must accept Writes as first parameter and <? extends Object> as second");

            if (!types.isSubtype(elements.getTypeElement(Writes.class.getCanonicalName()).asType(), params.get(0).asType()))
                utils.printError(symbol, "IO write must accept Writes as first parameter");

            read = false;
        }

        if (read) {
            if (create) return 2;
            else return 1;
        } else return 0;
    }

    public void processIOProviders() {
        var providers = roundEnv.getElementsAnnotatedWith(IOAnnotations.Provider.class);

        if (providers.isEmpty()) return;

        var providerMethodsCandidates = providers.stream().flatMap(e ->
                e instanceof TypeElement ?
                        e.getEnclosedElements().stream().filter(f ->
                                f instanceof Symbol.MethodSymbol && !utils.hasAnno(f, IOAnnotations.Provider.class) && !utils.hasAnno(f, IOAnnotations.Provider.UniProvider.class)
                        ).map(f -> (Symbol.MethodSymbol) f) :
                        e instanceof Symbol.MethodSymbol ?
                                Stream.of((Symbol.MethodSymbol) e) :
                                Stream.empty()
        ).filter(e -> e.getKind() == ElementKind.METHOD && !utils.hasAnno(e, IOAnnotations.Provider.Ignore.class) && e.getModifiers().contains(Modifier.STATIC)).collect(Collectors.toSet());

        // [write, read, readCreate]
        var candidatesMap = new LinkedHashMap<TypeMirror, Symbol.MethodSymbol[]>();
        for (Symbol.MethodSymbol candidate : providerMethodsCandidates) {
            var type = processIOProviderType(candidate);

            var returnType = candidate.getReturnType();
            var params = candidate.getParameters();

            utils.printNote(candidate, type);

            var ioTypeRaw = type == 1 || type == 2 ? returnType : params.get(1).asType();

            if (ioTypeRaw instanceof Type.TypeVar)
                utils.printError(candidate, "IO method must process at least class def, but " + ioTypeRaw + " given");

            var ioType = ioTypeRaw instanceof DeclaredType ? utils.getRawDeclaredTypeMirror((DeclaredType) ioTypeRaw) : ioTypeRaw;

            if (!candidatesMap.containsKey(ioType))
                candidatesMap.put(ioType, new Symbol.MethodSymbol[3]);

            var mas = candidatesMap.get(ioType);

            if (mas[type] != null)
                utils.printWarning("Multiple declaration of io method with signature " + returnType + " " + candidate);

            mas[type] = candidate;
        }

        for (TypeMirror type : candidatesMap.keySet()) {
            var mas = candidatesMap.get(type);

            if (ioProviders.containsKey(type))
                utils.printWarning("Redeclaration of io methods for type " + type);

            ioProviders.put(type, new CustomIOMethodProvider(
                    mas[0] == null ? null : utils.ident(utils.getTree(mas[0]).sym),
                    mas[1] == null ? null : utils.ident(utils.getTree(mas[1]).sym),
                    mas[2] == null ? null : utils.ident(utils.getTree(mas[2]).sym)
            ));
        }

        var keys = ioProviders.keySet();
        for (TypeMirror type : keys) {
            var provider = ioProviders.get(type);
            if (provider instanceof CustomIOMethodProvider) {
                if (((CustomIOMethodProvider) provider).readMethod == null)
                    utils.printWarning("read method does not set for " + type);
                if (((CustomIOMethodProvider) provider).writeMethod == null)
                    utils.printWarning("write method does not set for " + type);
                if (((CustomIOMethodProvider) provider).readCreateMethod == null)
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
                    utils.printNote("    |   write: " + (typeProvider.writeMethod == null ? "null" : typeProvider.writeMethod));
                    utils.printNote("    |   read: " + (typeProvider.readMethod == null ? "null" : typeProvider.readMethod));
                    utils.printNote("    |   readCreate: " + (typeProvider.readCreateMethod == null ? "null" : typeProvider.readCreateMethod));
                }
            }
            utils.printNote("IO uni methods:");
            uniProvidersTypes.forEach((m, t) -> {
                utils.printNote("    " + (t == 0 ? "read" : t == 1 ? "readCreate" : "write)") + " " + m.sym.getQualifiedName());
            });
            utils.printNote("--------|");
        }
    }

    public void processIOUniProviders() {
        var uniProviderCandidates = roundEnv.getElementsAnnotatedWith(IOAnnotations.Provider.UniProvider.class).stream()
                .map(m -> utils.getTree((ExecutableElement) m)).toList();

        for (JCTree.JCMethodDecl uniProviderCandidate : uniProviderCandidates)
            uniProvidersTypes.put(uniProviderCandidate, processIOProviderType(uniProviderCandidate.sym));

        var keys = uniProvidersTypes.keySet();
        for (JCTree.JCMethodDecl method : keys) {
            var stats = new LinkedList<JCTree.JCStatement>();

            var t = uniProvidersTypes.get(method);
            var symbol = method.sym;

            var jcParams = method.getParameters();

            var stream = jcParams.get(0);
            var obj = t != 0 ? jcParams.get(1) : null;

            if (t == 0) { // write

            } else if (t == 1) { // read

                utils.returnn(utils.ident(obj));
            } else if (t == 2) { // readCreate
                utils.returnn(utils.ident(obj));
            }

            method.body.stats = utils.listOf(stats);
        }
    }

    public void processIOGen() {
        Map<Symbol.ClassSymbol, Set<Symbol.VarSymbol>> map = new LinkedHashMap<>();

        var elementsForGenerate = roundEnv.getElementsAnnotatedWith(IOAnnotations.GenObject.Gen.class).stream()
                .filter(e -> e.getAnnotation(IOAnnotations.GenObject.Ignore.class) == null)
                .toList();

        for (Element element : elementsForGenerate) {
            if (element instanceof TypeElement) {
                var t = (Symbol.ClassSymbol) element;

                var set = new LinkedHashSet<Symbol.VarSymbol>();
                map.put(t, set);

                set.addAll(t.getEnclosedElements().stream()
                        .filter(e -> e.getAnnotation(IOAnnotations.GenObject.Ignore.class) == null && e instanceof VariableElement)
                        .map(e -> (Symbol.VarSymbol) e)
                        .toList());
            } else if (element instanceof VariableElement) {
                var v = (Symbol.VarSymbol) element;
                var t = (Symbol.ClassSymbol) v.getEnclosingElement();

                if (!map.containsKey(t))
                    map.put(t, new LinkedHashSet<>());

                var set = map.get(t);
                set.add(v);
            }
        }

        for (Symbol.ClassSymbol type : map.keySet()) {
            var fields = map.get(type);
            var typeTree = utils.getTree(type);
            var genObject = type.getAnnotation(IOAnnotations.GenObject.class);

            var superType = javacTypes.supertype(type.asType());

            utils.pos(typeTree.getMembers().last().pos + 1);

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

            typeTree.implementing = utils.listModified(typeTree.implementing, l -> {
                        l.removeIf(e -> {
                            utils.printNote(
                                    ((JCTree.JCIdent) e).sym.type,
                                    this.elements.getTypeElement("com.github.nekit508.annotations.ioproc.IO").asType(),
                                    ((JCTree.JCIdent) e).sym.type.equals(this.elements.getTypeElement("com.github.nekit508.annotations.ioproc.IO").asType())
                            );
                            return ((JCTree.JCIdent) e).sym.type.equals(this.elements.getTypeElement("com.github.nekit508.annotations.ioproc.IO").asType());
                        });
                    }
            );

            typeTree.defs = utils.listModified(typeTree.defs, l -> {
                LinkedList<JCTree.JCStatement> readBody = new LinkedList<>(), writeBody = new LinkedList<>();

                readBody.add(utils.exec(utils.executeMethodOnObject(treeMaker.Super(superType, type), "read", utils.listNil(), utils.listOf(utils.ident(utils.name("reads")).setType(utils.getTypeMirror(Reads.class))), symtab.voidType)));
                writeBody.add(utils.exec(utils.executeMethodOnObject(treeMaker.Super(superType, type), "write", utils.listNil(), utils.listOf(utils.ident(utils.name("writes")).setType(utils.getTypeMirror(Writes.class))), symtab.voidType)));

                for (Symbol.VarSymbol field : fields) {
                    var gen = field.getAnnotation(IOAnnotations.GenObject.Gen.class);

                    if (gen.autoCreation())
                        messager.printError("AutoCreation not implemented yet", field);

                    var provider = getIOMethodProvider(field.asType());

                    writeBody.add(
                            provider.getMethod(this,
                                    utils.ident(utils.name("writes")).setType(utils.classSymbol(Writes.class).type),
                                    utils.ident(utils.getTree(field)),
                                    field.asType(),
                                    false,
                                    gen.autoCreation()
                            )
                    );
                    readBody.add(
                            provider.getMethod(this,
                                    utils.ident(utils.name("reads")).setType(utils.classSymbol(Reads.class).type),
                                    utils.ident(utils.getTree(field)),
                                    field.asType(),
                                    true,
                                    gen.autoCreation()
                            )
                    );
                }

                var readMethod = utils.method(
                        utils.methodSymbol(Flags.PUBLIC, utils.name("read"), symtab.voidType, typeTree.sym),
                        utils.codeBlock(readBody)
                );
                readMethod.params = utils.listExtended(readMethod.params,
                        utils.paramVar(utils.ident(utils.classSymbol(Reads.class)), "reads", readMethod.sym)
                );
                readMethod.restype = utils.primitive(TypeTag.VOID);

                var writeMethod = utils.method(
                        utils.methodSymbol(Flags.PUBLIC, utils.name("write"), symtab.voidType, typeTree.sym),
                        utils.codeBlock(writeBody)
                );
                writeMethod.params = utils.listExtended(writeMethod.params,
                        utils.paramVar(utils.ident(utils.classSymbol(Writes.class)), "writes", writeMethod.sym)
                );

                writeMethod.restype = utils.primitive(TypeTag.VOID);

                readMethod.body = utils.codeBlock(readBody);
                writeMethod.body = utils.codeBlock(writeBody);

                l.add(writeMethod);
                l.add(readMethod);
            });

            Log.info(typeTree);
        }
    }

    public IOMethodProvider getIOMethodProvider(Type type) {
        var t = utils.unboxedTypeMirror(type instanceof DeclaredType ? utils.getRawDeclaredTypeMirror((DeclaredType) type) : type);
        utils.printNote(t, ioProviders.keySet().stream().map(TypeMirror::getClass).toList());
        return ioProviders.get(
                utils.unboxedTypeMirror(type instanceof DeclaredType ? utils.getRawDeclaredTypeMirror((DeclaredType) type) : type)
        );
    }
}
