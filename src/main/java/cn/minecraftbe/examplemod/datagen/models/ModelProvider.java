package cn.minecraftbe.examplemod.datagen.models;

import cn.minecraftbe.examplemod.ExampleMod;
import cn.minecraftbe.examplemod.block.Blocks;
import cn.minecraftbe.examplemod.item.Items;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jspecify.annotations.NonNull;

import java.util.Set;


@EventBusSubscriber(modid = ExampleMod.MODID)
public class ModelProvider extends net.minecraft.client.data.models.ModelProvider {

    public static final Set<DeferredItem<Item>> CUSTOM_ITEM = Set.of(Items.TEST_ITEM, Items.EXAMPLE_ITEM);
    public static final Set<DeferredBlock<Block>> CUSTOM_BLOCK = Set.of(cn.minecraftbe.examplemod.block.Blocks.TEST_MACHINE);

    public ModelProvider(PackOutput output) {
        super(output, ExampleMod.MODID);
    }

    @SubscribeEvent // on the mod event bus
    public static void gatherData(GatherDataEvent.Client event) {
        event.createProvider(ModelProvider::new);
    }

    private static boolean isCustom(Item item) {
        return CUSTOM_ITEM.stream().anyMatch(holder -> holder.get() == item);
    }

    private static boolean isCustom(Block block) {
        return CUSTOM_BLOCK.stream().anyMatch(holder -> holder.get() == block);
    }


    @Override
    protected void registerModels(@NonNull BlockModelGenerators blockModels, @NonNull ItemModelGenerators itemModels) {
        cn.minecraftbe.examplemod.block.Blocks.BLOCKS.getEntries().forEach(holder -> {
            if (!isCustom(holder.get())) blockModels.createTrivialBlock(holder.get(), TexturedModels.TEST_CUBE);
        });

        for (DeferredHolder<Item, ? extends Item> holder : Items.ITEMS.getEntries()) {
            var item = holder.get();
            if (item instanceof BlockItem) continue;
            if (isCustom(item)) continue;
            itemModels.generateFlatItem(item, net.minecraft.client.data.models.model.ModelTemplates.FLAT_ITEM);
        }

        // create block model manually
        ModelGenerators.createSimpleMachine(Blocks.TEST_MACHINE.get(), TexturedModels.SIMPLE_MACHINE, blockModels);

        // create item model manually
        ModelGenerators.generateTestItem(Items.TEST_ITEM.get(), net.minecraft.world.item.Items.FIREWORK_ROCKET, itemModels);
        ModelGenerators.generateTestItem(Items.EXAMPLE_ITEM.get(), net.minecraft.world.level.block.Blocks.STONE, itemModels);
    }
}
