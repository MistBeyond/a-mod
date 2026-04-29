package com.mistbeyond.examplemod.data.provider.model;

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


public class ModModelProvider extends net.minecraft.client.data.models.ModelProvider {
    private static final Set<ItemLike> CUSTOM_MODELS = Set.of(
            Init.REGISTRAR.block(Ids.CRUSHER), Init.REGISTRAR.block(Ids.TEST_MACHINE),
            Items.TEST_WRENCH, Items.EXAMPLE_ITEM
    );

    public ModModelProvider(PackOutput output) {
        super(output, Ids.MODID);
    }

    private static boolean isCustom(ItemLike item) {
        return CUSTOM_MODELS.stream().anyMatch(it -> it.asItem() == item.asItem());
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        for (var holder : Blocks.BLOCKS.getEntries()) {
            if (!isCustom(holder.get()))
                blockModels.createTrivialBlock(holder.get(), ModModels.TEST_CUBE);
        }

        for (var holder : Items.ITEMS.getEntries()) {
            var item = holder.get();
            if (!(item instanceof BlockItem || isCustom(item)))
                itemModels.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
        }

        ModelGenerators.createSimpleMachine(Init.REGISTRAR.block(Ids.CRUSHER), ModModels.SIMPLE_MACHINE, blockModels);
        ModelGenerators.createTestMachine(Init.REGISTRAR.block(Ids.TEST_MACHINE), ModModels.SIMPLE_MACHINE, blockModels);
        ModelGenerators.generateTestItem(Items.TEST_WRENCH.get(), FIREWORK_ROCKET, itemModels);
        ModelGenerators.generateTestItem(Items.EXAMPLE_ITEM.get(), STONE, itemModels);
    }
}
