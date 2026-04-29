package com.mistbeyond.examplemod.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class Ingredients {
    static final Extractors INGREDIENT_EXTRACTORS = new Extractors();

    private Ingredients() {
    }

    public static <I extends RecipeInput, R extends Recipe<I>> boolean isInputIn(RecipeMap recipes, DeferredHolder<RecipeType<?>, RecipeType<R>> type, ItemStack input) {
        var extractor = INGREDIENT_EXTRACTORS.get(type);
        if (extractor == null) {
            return false;
        }
        for (var holder : recipes.byType(type.get())) {
            if (extractor.testIngredient(holder.value(), input)) {
                return true;
            }
        }
        return false;
    }

    @FunctionalInterface
    public interface Extractor {
        @Nullable Ingredient extract(Recipe<?> recipe);

        default boolean testIngredient(Recipe<?> recipe, ItemStack input) {
            var ingredient = this.extract(recipe);
            return ingredient != null && ingredient.acceptsItem(input.typeHolder());
        }
    }

    public static class Extractors {
        private final Map<DeferredHolder<RecipeType<?>, ?>, Extractor> extractorMap = new HashMap<>();

        private Extractors() {
        }

        static Extractor forElectricSingleInput(DeferredHolder<RecipeType<?>, ? extends RecipeType<? extends ElectricSingleItemRecipe>> type) {
            return recipe -> recipe.getType() == type.get() && recipe instanceof ElectricSingleItemRecipe single ? single.input() : null;
        }

        public @Nullable Extractor get(DeferredHolder<RecipeType<?>, ?> key) {
            return extractorMap.get(key);
        }

        void register(DeferredHolder<RecipeType<?>, ?> typeHolder, Extractor extractor) {
            extractorMap.put(typeHolder, extractor);
        }
    }
}
