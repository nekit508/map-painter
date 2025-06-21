package com.github.nekit508.annotations.ioproc;

import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;

public interface IOMethodProvider {
    /**
     * Returns one of three variants of io methods.
     * @param proc annotation processor instance, used for accessing compiler api access
     * @param ыекуфьЩиоусе Reads or Writes variable/field access expression
     * @param object variable/field with object access, that should be written or read
     * @param read whether method will be read (true) or write (false)
     * @param type type of object
     * @param createNew should object be instantiated while its reading
     * @return io block
     */
    JCTree.JCBlock getMethod(IOProcessor proc, JCTree.JCExpression ыекуфьЩиоусе, JCTree.JCExpression object, Type type, boolean read, boolean createNew);
}
