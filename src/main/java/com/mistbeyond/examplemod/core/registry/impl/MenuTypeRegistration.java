package com.mistbeyond.examplemod.core.registry.impl;

import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;

public final class MenuTypeRegistration {
    private final String modId;
    private final HashMap<Identifier, DeferredHolder<MenuType<?>, ?>> modRegistry;
    private final DeferredRegister<MenuType<?>> gameRegistry;

    public MenuTypeRegistration(String modId, HashMap<Identifier, DeferredHolder<MenuType<?>, ?>> modRegistry, DeferredRegister<MenuType<?>> gameRegistry) {
        this.modId = modId;
        this.modRegistry = modRegistry;
        this.gameRegistry = gameRegistry;
    }

    public void register(String name, MenuType.MenuSupplier<?> supplier) {
        var id = Identifier.fromNamespaceAndPath(modId, name);
        if (modRegistry.containsKey(id)) {
            throw new IllegalStateException("MenuType '" + name + "' was registered twice.");
        }
        var holder = gameRegistry.register(name, () -> new MenuType<>(supplier, FeatureFlags.DEFAULT_FLAGS));
        modRegistry.put(id, holder);
    }
}
