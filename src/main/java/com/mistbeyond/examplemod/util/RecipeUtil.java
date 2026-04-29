package com.mistbeyond.examplemod.util;

import com.mistbeyond.examplemod.core.ModClientRecipes;
import com.mistbeyond.examplemod.recipe.Ingredients;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jspecify.annotations.Nullable;

public class RecipeUtil {
    private RecipeUtil() {
    }

    public static <I extends RecipeInput, R extends Recipe<I>> boolean isInputIn(Level level, DeferredHolder<RecipeType<?>, RecipeType<R>> type, ItemStack input) {
        if (level.isClientSide()) {
            return ModClientRecipes.isInputIn(type, input);
        }
        var serverLevel = (ServerLevel) level;
        return Ingredients.isInputIn(serverLevel.recipeAccess().recipeMap(), type, input);
    }

    public static @Nullable RecipeHolder<?> byKey(ResourceKey<Recipe<?>> key, Level level) {
        if (level.isClientSide()) {
            return ModClientRecipes.getRecipeMap().byKey(key);
        }
        var serverLevel = (ServerLevel) level;
        return serverLevel.recipeAccess().byKey(key).orElse(null);
    }
}
