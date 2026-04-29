package com.mistbeyond.examplemod.recipe.display;

import com.mistbeyond.examplemod.Ids;
import com.mistbeyond.examplemod.recipe.CrushingRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRecipeDisplays {
    public static final DeferredRegister<RecipeDisplay.Type<?>> RECIPE_DISPLAY_TYPES = DeferredRegister.create(BuiltInRegistries.RECIPE_DISPLAY, Ids.MODID);

    public static final DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<CrushingRecipeDisplay>> CRUSHING_RECIPE_DISPLAY = RECIPE_DISPLAY_TYPES.register(
            CrushingRecipe.ID, () -> new RecipeDisplay.Type<>(CrushingRecipeDisplay.MAP_CODEC, CrushingRecipeDisplay.STREAM_CODEC)
    );
}
