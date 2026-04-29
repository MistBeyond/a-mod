package com.mistbeyond.examplemod.recipe.display;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public record CrushingRecipeDisplay(
        SlotDisplay ingredient,
        SlotDisplay result,
        SlotDisplay craftingStation,
        long voltage,
        long current,
        int duration
) implements ElectricRecipeDisplay {
    public static final MapCodec<CrushingRecipeDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                    SlotDisplay.CODEC.fieldOf("ingredient").forGetter(CrushingRecipeDisplay::ingredient),
                    SlotDisplay.CODEC.fieldOf("result").forGetter(CrushingRecipeDisplay::result),
                    SlotDisplay.CODEC.fieldOf("craftingStation").forGetter(CrushingRecipeDisplay::craftingStation),
                    Codec.LONG.fieldOf("voltage").forGetter(CrushingRecipeDisplay::voltage),
                    Codec.LONG.fieldOf("current").forGetter(CrushingRecipeDisplay::current),
                    Codec.INT.fieldOf("duration").forGetter(CrushingRecipeDisplay::duration)
            ).apply(inst, CrushingRecipeDisplay::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, CrushingRecipeDisplay> STREAM_CODEC = StreamCodec.composite(
            SlotDisplay.STREAM_CODEC, CrushingRecipeDisplay::ingredient,
            SlotDisplay.STREAM_CODEC, CrushingRecipeDisplay::result,
            SlotDisplay.STREAM_CODEC, CrushingRecipeDisplay::craftingStation,
            ByteBufCodecs.VAR_LONG, CrushingRecipeDisplay::voltage,
            ByteBufCodecs.VAR_LONG, CrushingRecipeDisplay::current,
            ByteBufCodecs.VAR_INT, CrushingRecipeDisplay::duration,
            CrushingRecipeDisplay::new
    );

    @Override
    public Type<? extends RecipeDisplay> type() {
        return ModRecipeDisplays.CRUSHING_RECIPE_DISPLAY.get();
    }
}
