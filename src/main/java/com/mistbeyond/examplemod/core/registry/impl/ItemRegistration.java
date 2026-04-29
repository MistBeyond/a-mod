package com.mistbeyond.examplemod.core.registry.impl;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public final class ItemRegistration extends BaseRegistration<Item, Item.Properties, DeferredRegister.Items, DeferredItem<?>> {
    public ItemRegistration(String modId, HashMap<Identifier, DeferredItem<?>> modRegistry, DeferredRegister.Items gameRegistry) {
        super(modId, modRegistry, gameRegistry);
    }

    @Override
    public void register(String name, Function<Item.Properties, Item> func) {
        register(name, func, UnaryOperator.identity());
    }

    @Override
    public void register(String name, Function<Item.Properties, Item> func, UnaryOperator<Item.Properties> properties) {
        modRegistry.put(ID(name), gameRegistry.registerItem(name, func, properties));
    }
}
