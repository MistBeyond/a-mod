package com.mistbeyond.examplemod.integration.jei;

import com.mistbeyond.examplemod.Ids;
import com.mistbeyond.examplemod.Init;
import com.mistbeyond.examplemod.core.ModClientRecipes;
import com.mistbeyond.examplemod.integration.jei.recipe.CrushingCategory;
import com.mistbeyond.examplemod.integration.jei.recipe.ModJeiRecipeTypes;
import com.mistbeyond.examplemod.recipe.RecipeTypes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.Identifier;

@JeiPlugin
public class ModJeiPlugin implements IModPlugin {
    public static final Identifier UID = Ids.thisMod("jei_plugin");

    @Override
    public Identifier getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new CrushingCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (ModClientRecipes.isNotSynchronized()) {
            return;
        }
        var recipeMap = ModClientRecipes.getRecipeMap();

        var crushingRecipe = recipeMap.byType(RecipeTypes.CRUSHING.get()).stream().toList();
        registration.addRecipes(ModJeiRecipeTypes.CRUSHING, crushingRecipe);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(ModJeiRecipeTypes.CRUSHING, Init.REGISTRAR.item(Ids.CRUSHER));
    }
}
