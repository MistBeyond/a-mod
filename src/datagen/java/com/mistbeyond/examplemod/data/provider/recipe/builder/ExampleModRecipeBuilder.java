package com.mistbeyond.examplemod.data.provider.recipe.builder;

import com.mojang.logging.LogUtils;
import net.minecraft.advancements.Criterion;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeUnlockAdvancementBuilder;
import net.minecraft.world.item.crafting.Recipe;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public abstract class ExampleModRecipeBuilder implements RecipeBuilder {
    protected final Logger log = LogUtils.getLogger();
    protected final RecipeUnlockAdvancementBuilder advancementBuilder = new RecipeUnlockAdvancementBuilder();
    protected boolean showNotification = true;

    @Override
    public RecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.advancementBuilder.unlockedBy(name, criterion);
        return this;
    }

    @Override
    public RecipeBuilder group(@Nullable String group) {
        log.warn("Group is not supported");
        return this;
    }

    public ExampleModRecipeBuilder showNotification(boolean showNotification) {
        this.showNotification = showNotification;
        return this;
    }

    protected Recipe.CommonInfo getCommonInfo() {
        return new Recipe.CommonInfo(this.showNotification);
    }
}
