package com.mistbeyond.examplemod.recipe;

import com.mistbeyond.examplemod.Ids;
import com.mistbeyond.examplemod.Init;
import com.mistbeyond.examplemod.recipe.display.CrushingRecipeDisplay;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.List;

public class CrushingRecipe extends ElectricSingleItemRecipe {
    public static final String ID = "crushing";
    public static final MapCodec<CrushingRecipe> MAP_CODEC = simpleMapCodec(CrushingRecipe::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, CrushingRecipe> STREAM_CODEC = simpleStreamCodec(CrushingRecipe::new);

    public CrushingRecipe(CommonInfo commonInfo, ProcessInfo processInfo, Ingredient input, ItemStackTemplate result) {
        super(commonInfo, processInfo, input, result);
    }

    @Override
    public RecipeSerializer<? extends ElectricSingleItemRecipe> getSerializer() {
        return RecipeSerializers.CRUSHING.get();
    }

    @Override
    public RecipeType<? extends ElectricSingleItemRecipe> getType() {
        return RecipeTypes.CRUSHING.get();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(
                new CrushingRecipeDisplay(
                        input().display(),
                        new SlotDisplay.ItemStackSlotDisplay(result()),
                        new SlotDisplay.ItemSlotDisplay(Init.REGISTRAR.item(Ids.CRUSHER)),
                        processInfo.voltage(),
                        processInfo.current(),
                        processInfo.duration()
                )
        );
    }
}
