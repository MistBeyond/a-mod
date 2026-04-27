package cn.minecraftbe.examplemod.block.entity;


import cn.minecraftbe.examplemod.ExampleMod;
import cn.minecraftbe.examplemod.block.Blocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ExampleMod.MODID);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TestMachineBlockEntity>> TEST_MACHINE = BLOCK_ENTITIES.register(
            "test_machine",
            () -> new BlockEntityType<>(
                    TestMachineBlockEntity::new,
                    Blocks.TEST_MACHINE.get()
            )
    );
}
