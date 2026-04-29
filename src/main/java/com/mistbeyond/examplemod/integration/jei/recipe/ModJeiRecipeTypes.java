package com.mistbeyond.examplemod.integration.jei.recipe;

import com.mistbeyond.examplemod.recipe.CrushingRecipe;
import com.mistbeyond.examplemod.recipe.RecipeTypes;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import mezz.jei.api.recipe.types.IRecipeType;

public class ModJeiRecipeTypes {
    public static final IRecipeHolderType<CrushingRecipe> CRUSHING = IRecipeType.create(RecipeTypes.CRUSHING.get());
}
