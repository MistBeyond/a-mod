package com.mistbeyond.examplemod.recipe;

import com.mistbeyond.examplemod.core.VoltageTier;
import com.mistbeyond.examplemod.core.energy.ElectricProperty;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;

public interface ElectricRecipe<T extends RecipeInput> extends Recipe<T>, ElectricProperty.Provider<ElectricRecipe.ProcessInfo> {
    /**
     * For users, it is more explicit to invoke {@link ElectricRecipe#getProcessInfo()}.
     * For implementors, this method still needs to be implemented.
     */
    @Override
    ProcessInfo getElectricProperty();

    default ProcessInfo getProcessInfo() {
        return getElectricProperty();
    }

    record ProcessInfo(long voltage, long current, int duration) implements ElectricProperty.Common {
        public static final ProcessInfo LOW10s = new ProcessInfo(VoltageTier.LOW.value, 1, 200);
        public static final MapCodec<ProcessInfo> MAP_CODEC = ElectricProperty.simpleMapCodec(ProcessInfo::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, ProcessInfo> STREAM_CODEC = ElectricProperty.simpleStreamCodec(ProcessInfo::new);
    }
}
