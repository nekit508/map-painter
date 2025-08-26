package com.github.nekit508.mappainter.map.generator.statetypes.internal;

import arc.struct.ObjectMap;
import com.github.nekit508.Pair;
import mindustry.type.Item;
import mindustry.type.Liquid;

public class Balance {
    public ObjectMap<Item, Pair<Item, Integer>> items = new ObjectMap<>();
    public ObjectMap<Liquid, Pair<Liquid, Float>> liquids = new ObjectMap<>();

    public void add(Balance other) {
        var i = other.items.keys().toSeq().retainAll(item -> items.containsKey(item));
        var l = other.liquids.keys().toSeq().retainAll(liquid -> liquids.containsKey(liquid));

        for (var item : i)
            items.get(item).right += other.items.get(item).right;

        for (var liquid : l)
            liquids.get(liquid).right += other.liquids.get(liquid).right;
    }

    public void sub(Balance other) {
        var i = other.items.keys().toSeq().retainAll(item -> items.containsKey(item));
        var l = other.liquids.keys().toSeq().retainAll(liquid -> liquids.containsKey(liquid));

        for (var item : i)
            items.get(item).right -= other.items.get(item).right;

        for (var liquid : l)
            liquids.get(liquid).right -= other.liquids.get(liquid).right;
    }

    public boolean contains(Balance other) {
        var i = other.items.keys().toSeq().retainAll(item -> items.containsKey(item));
        var l = other.liquids.keys().toSeq().retainAll(liquid -> liquids.containsKey(liquid));

        for (var item : i)
            if (items.get(item).right < other.items.get(item).right)
                return false;

        for (var liquid : l)
            if (liquids.get(liquid).right < other.liquids.get(liquid).right)
                return false;

        return true;
    }

    public Balance delta(Balance other) {
        Balance out = new Balance();
        var i = other.items.keys().toSeq().retainAll(item -> items.containsKey(item));
        var l = other.liquids.keys().toSeq().retainAll(liquid -> liquids.containsKey(liquid));

        for (var item : i)
            if (items.get(item).right < other.items.get(item).right)
                out.items.put(item, new Pair<>(item, other.items.get(item).right - items.get(item).right));

        for (var liquid : l)
            if (liquids.get(liquid).right >= other.liquids.get(liquid).right)
                out.liquids.put(liquid, new Pair<>(liquid, other.liquids.get(liquid).right - liquids.get(liquid).right));

        return out;
    }
}
