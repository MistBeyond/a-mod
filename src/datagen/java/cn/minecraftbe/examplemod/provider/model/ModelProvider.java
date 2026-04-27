package cn.minecraftbe.examplemod.provider.model;

import cn.minecraftbe.examplemod.ExampleMod;
import cn.minecraftbe.examplemod.block.Blocks;
import cn.minecraftbe.examplemod.item.Items;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.ItemLike;

import java.util.Set;


public class ModelProvider extends net.minecraft.client.data.models.ModelProvider {
    private static final Set<ItemLike> CUSTOM_MODELS = Set.of(Blocks.TEST_MACHINE, Items.TEST_WRENCH, Items.EXAMPLE_ITEM);

    public ModelProvider(PackOutput output) {
        super(output, ExampleMod.MODID);
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

        ModelGenerators.createSimpleMachine(Blocks.TEST_MACHINE.get(), ModModels.SIMPLE_MACHINE, blockModels);
        ModelGenerators.generateTestItem(Items.TEST_WRENCH.get(), net.minecraft.world.item.Items.FIREWORK_ROCKET, itemModels);
        ModelGenerators.generateTestItem(Items.EXAMPLE_ITEM.get(), net.minecraft.world.level.block.Blocks.STONE, itemModels);
    }
}
