package com.github.nekit508.mappainter.map.generator.statetypes.internal;

import arc.struct.Seq;
import com.github.nekit508.mappainter.map.generator.StateType;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;

public class RecipeRule {
    public Seq<ItemStack> itemInputs = new Seq<>();
    public Seq<ItemStack> itemOutputs = new Seq<>();
    public Seq<LiquidStack> liquidInputs = new Seq<>();
    public Seq<LiquidStack> liquidOutputs = new Seq<>();

    public StateType rule;
}
