package com.mistbeyond.examplemod.integration.jei.recipe;

import com.mistbeyond.examplemod.Ids;
import com.mistbeyond.examplemod.Init;
import com.mistbeyond.examplemod.recipe.CrushingRecipe;
import com.mistbeyond.examplemod.recipe.display.CrushingRecipeDisplay;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;

public class CrushingCategory extends AbstractElectricSingleItemCategory<CrushingRecipe> {
    private static final Component TITLE = Component.translatable("gui.examplemod.category.crushing");

    public CrushingCategory(IGuiHelper guiHelper) {
        super(guiHelper, ModJeiRecipeTypes.CRUSHING, TITLE, Init.REGISTRAR.block(Ids.CRUSHER), 30);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<CrushingRecipe> recipeHolder, IFocusGroup focuses) {
        CrushingRecipe recipe = recipeHolder.value();
        var display = recipe.display().getFirst();
        if (display instanceof CrushingRecipeDisplay crushingRecipeDisplay) {
            builder.addInputSlot()
                    .setStandardSlotBackground()
                    .add(crushingRecipeDisplay.ingredient())
                    .setPosition(0, 0, getWidth() / 2, getHeight() - 40, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
            builder.addOutputSlot()
                    .setOutputSlotBackground()
                    .add(crushingRecipeDisplay.result())
                    .setPosition(getWidth() / 2, 0, getWidth() / 2, getHeight() - 40, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<CrushingRecipe> recipeHolder, IFocusGroup focuses) {
        super.createRecipeExtras(builder, recipeHolder, focuses);
        CrushingRecipe recipe = recipeHolder.value();
        var display = recipe.display().getFirst();
        if (display instanceof CrushingRecipeDisplay crushingRecipeDisplay) {
            builder.addAnimatedRecipeArrow(crushingRecipeDisplay.duration())
                    .setPosition(0, 0, getWidth(), getHeight() - 40, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        }
    }
}
