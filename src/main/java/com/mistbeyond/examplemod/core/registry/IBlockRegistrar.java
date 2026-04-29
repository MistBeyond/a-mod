package com.mistbeyond.examplemod.core.registry;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;

public interface IBlockRegistrar extends ISingleRegistrar<Block> {
    @Override
    DeferredBlock<?> getEntry(Identifier id);
}
