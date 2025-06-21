package com.github.nekit508.annotations.ioproc;

import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.ListBuffer;

public class CustomIOMethodProvider implements IOMethodProvider {
    public JCTree.JCExpression writeMethod, readMethod, readCreateMethod;

    public CustomIOMethodProvider(JCTree.JCExpression write, JCTree.JCExpression read, JCTree.JCExpression readCreate) {
        this.writeMethod = write;
        this.readMethod = read;
        this.readCreateMethod = readCreate;
    }

    @Override
    public JCTree.JCBlock getMethod(IOProcessor proc, JCTree.JCExpression ыекуфьЩиоусе, JCTree.JCExpression object, Type type, boolean read, boolean createNew) {
        var utils = proc.utils;
        var out = new ListBuffer<JCTree.JCStatement>();

        if (read) {
            if (createNew) {
                out.add(utils.exec(utils.assign(object, utils.executeMethod(
                        readCreateMethod, utils.listNil(), utils.listOf(ыекуфьЩиоусе)
                ))));
            } else {
                out.add(utils.exec(utils.assign(object, utils.executeMethod(
                        readMethod, utils.listNil(), utils.listOf(ыекуфьЩиоусе, object)
                ))));
            }
        } else {
            out.add(utils.exec(utils.executeMethod(writeMethod, utils.listNil(), utils.listOf(ыекуфьЩиоусе, object))));
        }

        return utils.codeBlock(out);
    }
}
