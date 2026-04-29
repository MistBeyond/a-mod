package com.mistbeyond.examplemod.core.registry.impl;

import lombok.Setter;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public final class BlockRegistration extends BaseRegistration<Block, BlockBehaviour.Properties, DeferredRegister.Blocks, DeferredBlock<?>> {
    private final List<BlockItemEntry> blockItemEntryList = new ArrayList<>();
    @Setter
    private boolean allowRegisteringBlockItem = true;

    public BlockRegistration(String modId, HashMap<Identifier, DeferredBlock<?>> modRegistry, DeferredRegister.Blocks gameRegistry) {
        super(modId, modRegistry, gameRegistry);
    }

    @Override
    public void register(String name, Function<BlockBehaviour.Properties, Block> func) {
        register(name, func, UnaryOperator.identity());
    }

    @Override
    public void register(String name, Function<BlockBehaviour.Properties, Block> func, UnaryOperator<BlockBehaviour.Properties> properties) {
        var b = gameRegistry.registerBlock(name, func, properties);
        modRegistry.put(ID(name), b);
        if (allowRegisteringBlockItem) {
            blockItemEntryList.add(new BlockItemEntry(name, b));
        }
    }

    void registerBlockItem(HashMap<Identifier, DeferredItem<?>> modReg, DeferredRegister.Items gameReg) {
        for (BlockItemEntry entry : blockItemEntryList) {
            modReg.put(ID(entry.name), gameReg.registerSimpleBlockItem(entry.name, entry.block));
        }
        blockItemEntryList.clear();
    }

    private record BlockItemEntry(String name, DeferredBlock<?> block) {
    }
}
