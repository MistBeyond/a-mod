package com.mistbeyond.examplemod.core.registry.impl;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.HashMap;

public final class ContainerScreenRegistration {
    private final String modId;
    private final HashMap<Identifier, MenuScreens.ScreenConstructor<?, ? extends AbstractContainerScreen<?>>> containerScreen;

    public ContainerScreenRegistration(String modId, HashMap<Identifier, MenuScreens.ScreenConstructor<?, ? extends AbstractContainerScreen<?>>> containerScreen) {
        this.modId = modId;
        this.containerScreen = containerScreen;
    }

    public <M extends AbstractContainerMenu, S extends AbstractContainerScreen<M>> void register(String name, MenuScreens.ScreenConstructor<M, S> constructor) {
        var id = Identifier.fromNamespaceAndPath(modId, name);
        if (containerScreen.put(id, constructor) != null) {
            throw new IllegalStateException("ContainerScreen '" + name + "' was registered twice.");
        }
    }
}
