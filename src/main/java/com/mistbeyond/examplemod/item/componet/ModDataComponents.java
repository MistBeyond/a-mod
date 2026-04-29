package com.mistbeyond.examplemod.item.componet;

import com.mistbeyond.examplemod.Ids;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENT_TYPES = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Ids.MODID);
    public static final Supplier<DataComponentType<Long>> ENERGY = DATA_COMPONENT_TYPES.registerComponentType(
            "energy", builder -> builder.persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG)
    );
}
