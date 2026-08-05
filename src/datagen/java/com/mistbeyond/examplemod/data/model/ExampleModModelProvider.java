package com.mistbeyond.examplemod.data.model;

import com.mistbeyond.examplemod.Ids;
import com.mistbeyond.examplemod.Init;
import com.mistbeyond.examplemod.block.Blocks;
import com.mistbeyond.examplemod.item.Items;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.ItemLike;

import java.util.Set;

import static net.minecraft.world.item.Items.FIREWORK_ROCKET;
import static net.minecraft.world.level.block.Blocks.STONE;


public class ExampleModModelProvider extends net.minecraft.client.data.models.ModelProvider {
    private static final Set<ItemLike> CUSTOM_MODELS = Set.of(
            Init.REGISTRAR.block(Ids.CRUSHER), Init.REGISTRAR.block(Ids.TEST_MACHINE),
            Init.REGISTRAR.block(Ids.WIRE),
            Items.TEST_WRENCH, Items.EXAMPLE_ITEM
    );

    public ExampleModModelProvider(PackOutput output) {
        super(output, Ids.MODID);
    }

    private static boolean isCustom(ItemLike item) {
        return CUSTOM_MODELS.stream().anyMatch(it -> it.asItem() == item.asItem());
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        for (var holder : Blocks.BLOCKS.getEntries()) {
            if (!isCustom(holder.get()))
                blockModels.createTrivialBlock(holder.get(), ExampleModModels.TEST_CUBE);
        }

        for (var holder : Items.ITEMS.getEntries()) {
            var item = holder.get();
            if (!(item instanceof BlockItem || isCustom(item)))
                itemModels.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
        }

        ExampleModModelGenerators.createSimpleMachine(Init.REGISTRAR.block(Ids.CRUSHER), ExampleModModels.SIMPLE_MACHINE, blockModels);
        ExampleModModelGenerators.createTestMachine(Init.REGISTRAR.block(Ids.TEST_MACHINE), ExampleModModels.SIMPLE_MACHINE, blockModels);
        ExampleModModelGenerators.createWire(Init.REGISTRAR.block(Ids.WIRE), blockModels);
        ExampleModModelGenerators.createWireItem(Init.REGISTRAR.item(Ids.WIRE), itemModels);
        ExampleModModelGenerators.generateTestItem(Items.TEST_WRENCH.get(), FIREWORK_ROCKET, itemModels);
        ExampleModModelGenerators.generateTestItem(Items.EXAMPLE_ITEM.get(), STONE, itemModels);
    }
}
