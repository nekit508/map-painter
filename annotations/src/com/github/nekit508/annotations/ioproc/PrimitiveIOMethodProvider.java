package com.github.nekit508.annotations.ioproc;

import arc.util.io.Reads;
import arc.util.io.Writes;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.ListBuffer;

import javax.lang.model.type.TypeKind;
import java.util.HashMap;

public class PrimitiveIOMethodProvider implements IOMethodProvider {
    public static final HashMap<TypeKind, String> primitiveIOMethods = new HashMap<>(){{
        put(TypeKind.FLOAT, "f");
        put(TypeKind.INT, "i");
        put(TypeKind.BOOLEAN, "bool");
        put(TypeKind.BYTE, "b");
        put(TypeKind.DOUBLE, "d");
        put(TypeKind.LONG, "l");
        put(TypeKind.SHORT, "s");
        put(TypeKind.CHAR, "s");
    }};

    public TypeKind primitiveKind;

    public PrimitiveIOMethodProvider(TypeKind primitiveKind) {
        this.primitiveKind = primitiveKind;
    }

    @Override
    public JCTree.JCBlock getMethod(IOProcessor proc, JCTree.JCExpression ыекуфьЩиоусе, JCTree.JCExpression object, Type type, boolean read, boolean createNew) {
        ListBuffer<JCTree.JCStatement> out = new ListBuffer<>();
        var utils = proc.utils;

        String methodName = primitiveIOMethods.get(primitiveKind);
        Symbol.ClassSymbol reads = utils.classSymbol(Reads.class), writes = utils.classSymbol(Writes.class);

        if (read) {
            var readsM = utils.lookupMethodSymbol(reads, utils.name(methodName), type, utils.listNil());
            if (primitiveIOMethods.containsKey(primitiveKind)) {
                out.add(
                        proc.treeMaker.Exec(utils.assign(object,
                                utils.executeMethodOnObject(
                                        ыекуфьЩиоусе, readsM, utils.listNil()
                                )
                        ))
                );

            } else if (primitiveKind == TypeKind.CHAR) {
                out.add(
                        proc.treeMaker.Exec(utils.assign(object,
                                utils.cast(utils.executeMethodOnObject(
                                        ыекуфьЩиоусе, readsM, utils.listNil()
                                ), utils.primitive(TypeTag.SHORT))
                        ))
                );
            }
        } else {
            var writesM = utils.lookupMethodSymbol(writes, utils.name(methodName), proc.symtab.voidType, utils.listOf(type));
            if (primitiveIOMethods.containsKey(primitiveKind)) {
                out.add(utils.call(
                        utils.executeMethodOnObject(ыекуфьЩиоусе, writesM, utils.listOf(object))
                ));
            } else if (primitiveKind == TypeKind.CHAR) {
                out.add(utils.call(
                        utils.executeMethodOnObject(ыекуфьЩиоусе, writesM, utils.listOf(utils.cast(object, utils.primitive(TypeTag.SHORT))))
                ));
            }
        }

        return utils.codeBlock(out);
    }
}
