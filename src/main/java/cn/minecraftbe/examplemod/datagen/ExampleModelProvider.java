package cn.minecraftbe.examplemod.datagen;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jspecify.annotations.NonNull;

import static cn.minecraftbe.examplemod.ExampleMod.MODID;
import static cn.minecraftbe.examplemod.block.Blocks.BLOCKS;
import static cn.minecraftbe.examplemod.item.Items.ITEMS;
import static net.minecraft.client.data.models.model.TextureMapping.getBlockTexture;

@EventBusSubscriber(modid = MODID)
public class ExampleModelProvider extends ModelProvider {
    public static final TexturedModel.Provider TEST_CUBE = TexturedModel.createDefault(ExampleModelProvider::testCube, ModelTemplates.CUBE_ALL);

    public ExampleModelProvider(PackOutput output) {
        super(output, MODID);
    }

    @SubscribeEvent // on the mod event bus
    public static void gatherData(GatherDataEvent.Client event) {
        event.createProvider(ExampleModelProvider::new);
    }

    public static TextureMapping testCube(Block block) {
        Material texture = getBlockTexture(Blocks.IRON_BLOCK);
        return TextureMapping.cube(texture);
    }

    @Override
    protected void registerModels(@NonNull BlockModelGenerators blockModels, @NonNull ItemModelGenerators itemModels) {

        BLOCKS.getEntries().forEach(holder ->
                blockModels.createTrivialBlock(holder.get(), TEST_CUBE)
        );

        for (DeferredHolder<Item, ? extends Item> holder : ITEMS.getEntries()) {
            var item = holder.get();
            if (item instanceof BlockItem) continue;
            itemModels.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
        }
    }
}
