package com.github.nekit508.annotations;

import com.github.nekit508.annotations.utils.Utils;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.TypeAnnotations;
import com.sun.tools.javac.comp.Resolve;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.model.JavacTypes;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.processing.JavacFiler;
import com.sun.tools.javac.processing.JavacMessager;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.processing.JavacRoundEnvironment;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.*;
import javax.lang.model.element.TypeElement;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class BaseProcessor extends AbstractProcessor {
    public JavacTypes types;
    public JavacElements elements;
    public JavacFiler filer;
    public JavacMessager messager;
    public JavacRoundEnvironment roundEnv;

    public Context context;

    public Trees trees;
    public TreeMaker treeMaker;
    public Names names;
    public com.sun.tools.javac.code.Types javacTypes;
    public ParserFactory parserFactory;
    public TypeAnnotations typeAnnotations;
    public Symtab symtab;
    public Resolve resolve;

    public Utils utils;

    public JavacProcessingEnvironment javacProcessingEnvironment;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        this.roundEnv = (JavacRoundEnvironment) roundEnv;
        return false;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        javacProcessingEnvironment = (JavacProcessingEnvironment) processingEnv;

        types = javacProcessingEnvironment.getTypeUtils();
        elements = javacProcessingEnvironment.getElementUtils();
        filer = javacProcessingEnvironment.getFiler();
        messager = (JavacMessager) javacProcessingEnvironment.getMessager();

        context = javacProcessingEnvironment.getContext();

        trees = Trees.instance(javacProcessingEnvironment);
        treeMaker = TreeMaker.instance(context);
        names = Names.instance(context);
        javacTypes = com.sun.tools.javac.code.Types.instance(context);
        parserFactory = ParserFactory.instance(context);
        typeAnnotations = TypeAnnotations.instance(context);
        symtab = Symtab.instance(context);
        resolve = Resolve.instance(context);

        utils = new Utils(this);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> out = new LinkedHashSet<>();

        var types = getClass().getAnnotation(NSupportedAnnotationTypes.class).value();
        for (Class<?> type : types)
            out.add(type.getCanonicalName());

        return out;
    }
}
