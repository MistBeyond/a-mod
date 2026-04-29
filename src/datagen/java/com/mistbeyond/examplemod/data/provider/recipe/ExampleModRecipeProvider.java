package com.mistbeyond.examplemod.data.provider.recipe;

import com.mistbeyond.examplemod.data.provider.recipe.builder.CrushingRecipeBuilder;
import com.mistbeyond.examplemod.recipe.ElectricRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.concurrent.CompletableFuture;

public class ExampleModRecipeProvider extends RecipeProvider {
    protected ExampleModRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        new CrushingRecipeBuilder(
                Ingredient.of(Items.RAW_IRON),
                new ItemStackTemplate(com.mistbeyond.examplemod.item.Items.CRUSHED_IRON_ORE, 2),
                ElectricRecipe.ProcessInfo.LOW10s
        )
                .unlockedBy("has_iron_ore", has(Items.RAW_IRON))
                .save(output);
    }

    public static class Runner extends RecipeProvider.Runner {

        public Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
            super(packOutput, registries);
        }

        @Override
        public String getName() {
            return "Example Mod Recipes";
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new ExampleModRecipeProvider(registries, output);
        }
    }
}
