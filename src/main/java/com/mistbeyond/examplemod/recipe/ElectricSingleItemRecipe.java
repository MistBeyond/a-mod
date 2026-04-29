package com.mistbeyond.examplemod.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public abstract class ElectricSingleItemRecipe implements ElectricRecipe<SingleRecipeInput> {
    protected final Recipe.CommonInfo commonInfo;
    protected final ProcessInfo processInfo;
    private final Ingredient input;
    private final ItemStackTemplate result;
    private @Nullable PlacementInfo placementInfo;

    public ElectricSingleItemRecipe(CommonInfo commonInfo, ProcessInfo processInfo, Ingredient input, ItemStackTemplate result) {
        this.commonInfo = commonInfo;
        this.processInfo = processInfo;
        this.input = input;
        this.result = result;
    }

    public static <T extends ElectricSingleItemRecipe> MapCodec<T> simpleMapCodec(Factory<T> factory) {
        return RecordCodecBuilder.mapCodec(inst -> inst.group(
                CommonInfo.MAP_CODEC.forGetter(r -> r.commonInfo),
                ProcessInfo.MAP_CODEC.forGetter(r -> r.processInfo),
                Ingredient.CODEC.fieldOf("ingredient").forGetter(ElectricSingleItemRecipe::input),
                ItemStackTemplate.CODEC.fieldOf("result").forGetter(ElectricSingleItemRecipe::result)
        ).apply(inst, factory::create));
    }

    public static <T extends ElectricSingleItemRecipe> StreamCodec<RegistryFriendlyByteBuf, T> simpleStreamCodec(Factory<T> factory) {
        return StreamCodec.composite(
                CommonInfo.STREAM_CODEC, r -> r.commonInfo,
                ProcessInfo.STREAM_CODEC, r -> r.processInfo,
                Ingredient.CONTENTS_STREAM_CODEC, ElectricSingleItemRecipe::input,
                ItemStackTemplate.STREAM_CODEC, ElectricSingleItemRecipe::result,
                factory::create
        );
    }

    @Override
    public ProcessInfo getElectricProperty() {
        return processInfo;
    }

    public Ingredient input() {
        return this.input;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return this.input.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        return this.result.create();
    }

    @Override
    public boolean showNotification() {
        return this.commonInfo.showNotification();
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public abstract RecipeSerializer<? extends ElectricSingleItemRecipe> getSerializer();

    @Override
    public abstract RecipeType<? extends ElectricSingleItemRecipe> getType();

    @Override
    public PlacementInfo placementInfo() {
        if (this.placementInfo == null) {
            this.placementInfo = PlacementInfo.create(this.input);
        }

        return this.placementInfo;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.BLAST_FURNACE_BLOCKS;
    }

    protected ItemStackTemplate result() {
        return this.result;
    }

    @FunctionalInterface
    public interface Factory<T extends ElectricSingleItemRecipe> {
        T create(CommonInfo commonInfo, ProcessInfo processInfo, Ingredient input, ItemStackTemplate result);
    }
}
