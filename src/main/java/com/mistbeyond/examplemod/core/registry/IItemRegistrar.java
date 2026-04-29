package com.mistbeyond.examplemod.core.registry;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

public interface IItemRegistrar extends ISingleRegistrar<Item> {
    @Override
    DeferredItem<?> getEntry(Identifier id);
}
