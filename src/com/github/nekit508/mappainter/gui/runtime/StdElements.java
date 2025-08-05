package com.github.nekit508.mappainter.gui.runtime;

import arc.func.Func3;
import arc.scene.Element;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;

public class StdElements {
    public static ObjectMap<String, Func3<Table, ObjectMap<String, Object>, Object[], Cell<Element>>> functions = new ObjectMap<>();

    static {
        functions.put("table", (table, kwargs, args) -> {
            var out = new Table();
            return table.add(out);
        });
        functions.put("label", (table, kwargs, args) -> {
            var label = table.labelWrap((String) args[0]);
            return as(label);
        });
    }

    // TODO make cell optional and replace Table with WidgetGroup
    public static <T extends Element> Cell<T> elementDecl(String method, Table parent, ObjectMap<String, Object> kwargs, Object[] params) {
        return (Cell<T>) functions.get(method).get(parent, kwargs, params);
    }

    public static <T> T as(Object obj) {
        return (T) obj;
    }
}
