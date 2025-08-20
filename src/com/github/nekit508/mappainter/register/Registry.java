package com.github.nekit508.mappainter.register;

import arc.func.Prov;
import arc.struct.ObjectMap;

public class Registry<O> {
    public final String name;
    public ObjectMap<String, RegistryObject<? extends O>> registryObjectsMap = new ObjectMap<>();

    public Registry(String name) {
        this.name = name;
    }

    public <R extends O> RegistryObject<R> register(String name, Prov<R> resolver) {
        var out = new RegistryObject<>(name, resolver);
        registryObjectsMap.put(out.name, out);
        return out;
    }

    public void resolve() {
        var registryObjects = registryObjectsMap.values();
        for (var registryObject : registryObjects)
            registryObject.resolveObject();
    }

    public <R extends O> O get(String name) {
        return (R) registryObjectsMap.get(name);
    }
}
