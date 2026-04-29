package com.mistbeyond.examplemod.block;

import com.mistbeyond.examplemod.Ids;
import com.mistbeyond.examplemod.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;
import java.util.function.UnaryOperator;

public final class Blocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Ids.MODID);
    public static final DeferredBlock<Block> EXAMPLE_BLOCK = registerSimpleWithBlockItem(
            "example_block", p -> p.mapColor(MapColor.STONE)
    );

    public static DeferredBlock<Block> registerSimpleWithBlockItem(String name, UnaryOperator<BlockBehaviour.Properties> properties) {
        var ret = BLOCKS.registerSimpleBlock(name, properties);
        Items.ITEMS.registerSimpleBlockItem(name, ret);
        return ret;
    }

    public static <B extends Block> DeferredBlock<B> registerWithBlockItem(String name, Function<BlockBehaviour.Properties, B> func, UnaryOperator<BlockBehaviour.Properties> properties) {
        var ret = BLOCKS.registerBlock(name, func, properties);
        Items.ITEMS.registerSimpleBlockItem(name, ret);
        return ret;
    }
}
