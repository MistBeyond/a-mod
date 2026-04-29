package com.mistbeyond.examplemod.data.provider.recipe.builder;

import com.mistbeyond.examplemod.recipe.CrushingRecipe;
import com.mistbeyond.examplemod.recipe.ElectricRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

public class CrushingRecipeBuilder extends ExampleModRecipeBuilder {
    private final Ingredient input;
    private final ItemStackTemplate result;
    private final ElectricRecipe.ProcessInfo processInfo;

    public CrushingRecipeBuilder(Ingredient input, ItemStackTemplate result, ElectricRecipe.ProcessInfo processInfo) {
        this.input = input;
        this.result = result;
        this.processInfo = processInfo;
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return RecipeBuilder.getDefaultRecipeId(result);
    }

    @Override
    public void save(RecipeOutput output, ResourceKey<Recipe<?>> key) {
        var recipe = new CrushingRecipe(getCommonInfo(), processInfo, input, result);
        output.accept(key, recipe, advancementBuilder.build(output, key, (String) null));
    }
}
