package com.mistbeyond.examplemod.integration.jei.recipe;

import com.mistbeyond.examplemod.recipe.ElectricSingleItemRecipe;
import com.mistbeyond.examplemod.recipe.display.ElectricRecipeDisplay;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;

public abstract class AbstractElectricSingleItemCategory<T extends ElectricSingleItemRecipe> extends AbstractElectricRecipe<T> {
    public AbstractElectricSingleItemCategory(IGuiHelper guiHelper, IRecipeType<RecipeHolder<T>> recipeType, Component title, ItemLike icon, int height) {
        super(guiHelper, recipeType, title, icon, height);
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<T> recipeHolder, IFocusGroup focuses) {
        T recipe = recipeHolder.value();
        var display = recipe.display().getFirst();
        if (display instanceof ElectricRecipeDisplay electricRecipeDisplay) {
            super.createRecipeExtras(builder, recipeHolder, focuses);
            addEnergyInfo(builder, electricRecipeDisplay);
        }
    }
}
