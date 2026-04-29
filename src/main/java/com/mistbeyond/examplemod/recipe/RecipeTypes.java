package com.mistbeyond.examplemod.recipe;

import com.mistbeyond.examplemod.Ids;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class RecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, Ids.MODID);
    private static final List<DeferredHolder<RecipeType<?>, RecipeType<?>>> all = new ArrayList<>();
    public static final DeferredHolder<RecipeType<?>, RecipeType<CrushingRecipe>> CRUSHING = registerWith(CrushingRecipe.ID, Ingredients.Extractors::forElectricSingleInput);

    private RecipeTypes() {
    }

    public static List<DeferredHolder<RecipeType<?>, RecipeType<?>>> getAll() {
        return List.copyOf(all);
    }

    private static <T extends Recipe<?>> DeferredHolder<RecipeType<?>, RecipeType<T>> register(String id) {
        DeferredHolder<RecipeType<?>, RecipeType<T>> ret = RECIPE_TYPES.register(id, RecipeType::simple);
        all.add(unsafeCast(ret));
        return ret;
    }

    private static <T extends Recipe<?>> DeferredHolder<RecipeType<?>, RecipeType<T>> registerWith(String id, Ingredients.Extractor extractor) {
        DeferredHolder<RecipeType<?>, RecipeType<T>> ret = register(id);
        Ingredients.INGREDIENT_EXTRACTORS.register(ret, extractor);
        return ret;
    }

    private static <T extends Recipe<?>> DeferredHolder<RecipeType<?>, RecipeType<T>> registerWith(String id, Function<DeferredHolder<RecipeType<?>, RecipeType<T>>, Ingredients.Extractor> extractorFactory) {
        DeferredHolder<RecipeType<?>, RecipeType<T>> ret = register(id);
        Ingredients.INGREDIENT_EXTRACTORS.register(ret, extractorFactory.apply(ret));
        return ret;
    }

    @SuppressWarnings("unchecked")
    private static <T> T unsafeCast(Object o) {
        return (T) o;
    }
}
