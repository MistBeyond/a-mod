package com.mistbeyond.examplemod.core;

import com.mistbeyond.examplemod.recipe.Ingredients;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Stores recipes, generally from vanilla and this mod.
 */
@Slf4j
public final class ModClientRecipes {
    private static RecipeMap recipes = RecipeMap.EMPTY;

    private ModClientRecipes() {
    }

    public static boolean isNotSynchronized() {
        return recipes == RecipeMap.EMPTY;
    }

    public static RecipeMap getRecipeMap() {
        if (isNotSynchronized()) {
            log.warn("Client recipes are not synchronous");
        }
        return recipes;
    }

    public static <I extends RecipeInput, R extends Recipe<I>> boolean isInputIn(DeferredHolder<RecipeType<?>, RecipeType<R>> type, ItemStack input) {
        return Ingredients.isInputIn(recipes, type, input);
    }

    static void syncRecipe(RecipeMap recipes) {
        ModClientRecipes.recipes = recipes;
    }

    static void clearRecipe() {
        recipes = RecipeMap.EMPTY;
    }
}
