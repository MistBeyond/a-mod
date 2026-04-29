package com.mistbeyond.examplemod.integration.jei.recipe;

import com.mistbeyond.examplemod.core.VoltageTier;
import com.mistbeyond.examplemod.recipe.ElectricRecipe;
import com.mistbeyond.examplemod.recipe.display.ElectricRecipeDisplay;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;

public abstract class AbstractElectricRecipe<T extends ElectricRecipe<?>> extends AbstractRecipeCategory<RecipeHolder<T>> {
    public AbstractElectricRecipe(IGuiHelper guiHelper, IRecipeType<RecipeHolder<T>> recipeType, Component title, ItemLike icon, int height) {
        super(recipeType, title, guiHelper.createDrawableItemLike(icon), 130, height + 40);
    }

    protected void addEnergyInfo(IRecipeExtrasBuilder builder, ElectricRecipeDisplay recipe) {
        VoltageTier v = recipe.voltageTier();
        builder.addText(Component.translatable("gui.examplemod.energy.voltage", recipe.voltage() + "V", v.color + v.name() + "§r"), getWidth(), getHeight() - 30)
                .setColor(0xFF808080)
                .setTextAlignment(HorizontalAlignment.RIGHT)
                .setTextAlignment(VerticalAlignment.BOTTOM);

        builder.addText(Component.translatable("gui.examplemod.energy.current", recipe.current() + "A"), getWidth(), getHeight() - 20)
                .setColor(0xFF808080)
                .setTextAlignment(HorizontalAlignment.RIGHT)
                .setTextAlignment(VerticalAlignment.BOTTOM);

        builder.addText(Component.translatable("gui.examplemod.energy.power", recipe.power() + "EU/t"), getWidth(), getHeight() - 10)
                .setColor(0xFF808080)
                .setTextAlignment(HorizontalAlignment.RIGHT)
                .setTextAlignment(VerticalAlignment.BOTTOM);

        builder.addText(Component.translatable("gui.examplemod.energy.consumption", recipe.totalEnergy() + "EU"), getWidth(), getHeight())
                .setColor(0xFF808080)
                .setTextAlignment(HorizontalAlignment.RIGHT)
                .setTextAlignment(VerticalAlignment.BOTTOM);
    }
}
