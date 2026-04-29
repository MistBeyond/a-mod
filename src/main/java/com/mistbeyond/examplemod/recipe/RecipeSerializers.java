package com.mistbeyond.examplemod.recipe;

import com.mistbeyond.examplemod.Ids;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class RecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, Ids.MODID);
    public static final Supplier<RecipeSerializer<CrushingRecipe>> CRUSHING = RECIPE_SERIALIZERS.register(
            CrushingRecipe.ID,
            () -> new RecipeSerializer<>(CrushingRecipe.MAP_CODEC, CrushingRecipe.STREAM_CODEC)
    );

    private RecipeSerializers() {
    }
}
