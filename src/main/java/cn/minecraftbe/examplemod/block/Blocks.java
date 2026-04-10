package cn.minecraftbe.examplemod.block;

import cn.minecraftbe.examplemod.ExampleMod;
import cn.minecraftbe.examplemod.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;
import java.util.function.UnaryOperator;


public class Blocks {
    // Create a Deferred Register to hold Blocks which will all be registered under the "examplemod" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ExampleMod.MODID);

    public static final DeferredBlock<Block> TEST_MACHINE = registerWithBlockItem(
            "test_machine",
            TestMachine::new,
            p -> p.sound(SoundType.NETHERITE_BLOCK)
    );
    public static final DeferredBlock<Block> EXAMPLE_BLOCK = registerSimpleWithBlockItem(
            "example_block",
            p -> p.mapColor(MapColor.STONE)
    );

    public static DeferredBlock<Block> registerSimpleWithBlockItem(
            String name, UnaryOperator<BlockBehaviour.Properties> properties
    ) {
        var ret = BLOCKS.registerSimpleBlock(name, properties);
        Items.ITEMS.registerSimpleBlockItem(name, ret);
        return ret;
    }

    public static <B extends Block> DeferredBlock<B> registerWithBlockItem(
            String name, Function<BlockBehaviour.Properties, B> func
    ) {

        var ret = BLOCKS.registerBlock(name, func);
        Items.ITEMS.registerSimpleBlockItem(name, ret);
        return ret;
    }


    public static <B extends Block> DeferredBlock<B> registerWithBlockItem(
            String name,
            Function<BlockBehaviour.Properties, B> func,
            UnaryOperator<BlockBehaviour.Properties> properties
    ) {
        var ret = BLOCKS.registerBlock(name, func, properties);
        Items.ITEMS.registerSimpleBlockItem(name, ret);
        return ret;
    }
}
