package com.mistbeyond.examplemod.core.registry.impl;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public sealed abstract class BaseRegistration<T, P, R extends DeferredRegister<T>, H extends DeferredHolder<T, ?>>
        permits BlockRegistration, ItemRegistration {
    protected final HashMap<Identifier, H> modRegistry;
    protected final R gameRegistry;
    protected final String modId;

    public BaseRegistration(String modId, HashMap<Identifier, H> modRegistry, R gameRegistry) {
        this.modId = modId;
        this.modRegistry = modRegistry;
        this.gameRegistry = gameRegistry;
    }

    public abstract void register(String name, Function<P, T> func);

    public abstract void register(String name, Function<P, T> func, UnaryOperator<P> properties);

//    public abstract void register(String name, Supplier<? extends T> sup);

    protected Identifier ID(String name) {
        return Identifier.fromNamespaceAndPath(modId, name);
    }

    protected Identifier ID(String modid, String name) {
        return Identifier.fromNamespaceAndPath(modid, name);
    }
}

