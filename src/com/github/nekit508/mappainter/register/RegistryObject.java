package com.github.nekit508.mappainter.register;

import arc.func.Prov;

public final class RegistryObject<O> {
    public final String name;
    public Prov<O> objectResolver;
    private O resolvedObject;

    public RegistryObject(String name, Prov<O> objectResolver) {
        this.name = name;
        this.objectResolver = objectResolver;
    }

    void resolveObject() {
        if (resolvedObject == null)
            resolvedObject = objectResolver.get();
        else
            throw new RuntimeException("Second resolution of " + name + ".");
    }

    public O get() {
        if (resolvedObject == null)
            throw new RuntimeException("Object is still unresolved in " + name + ".");
        return resolvedObject;
    }
}
