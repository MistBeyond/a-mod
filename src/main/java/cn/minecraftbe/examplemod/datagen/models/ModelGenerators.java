package cn.minecraftbe.examplemod.datagen.models;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class ModelGenerators {
    public static void generateTestItem(Item acceptor, ItemLike donor, ItemModelGenerators generator) {
        TextureMapping mapping;
        if (donor instanceof Block)
            mapping = TextureMapping.layer0((Block) donor);
        else if (donor instanceof Item)
            mapping = TextureMapping.layer0((Item) donor);
        else
            throw new IllegalArgumentException("Unsupported donor type: " + donor.getClass());
        generator.itemModelOutput.accept(
                acceptor,
                ItemModelUtils.plainModel(net.minecraft.client.data.models.model.ModelTemplates.FLAT_ITEM.create(
                        acceptor,
                        mapping,
                        generator.modelOutput
                )));
    }

    public static void createSimpleMachine(Block machine, TexturedModel.Provider provider, BlockModelGenerators generator) {
        Material frontTextureOn = TextureMapping.getBlockTexture(net.minecraft.world.level.block.Blocks.BLAST_FURNACE, "_front_on");
        Material frontTextureOff = TextureMapping.getBlockTexture(net.minecraft.world.level.block.Blocks.BLAST_FURNACE, "_front");
        Material sideTexture = TextureMapping.getBlockTexture(net.minecraft.world.level.block.Blocks.IRON_BLOCK);

        MultiVariant normalModel = BlockModelGenerators.plainVariant(
                provider.get(machine).updateTextures(t -> t.put(TextureSlot.FRONT, frontTextureOff))
                        .updateTextures(t -> t.put(TextureSlot.SIDE, sideTexture))
                        .create(machine, generator.modelOutput)
        );
        MultiVariant litModel = BlockModelGenerators.plainVariant(
                provider.get(machine).updateTextures(t -> t.put(TextureSlot.FRONT, frontTextureOn))
                        .updateTextures(t -> t.put(TextureSlot.SIDE, sideTexture))
                        .createWithSuffix(machine, "_on", generator.modelOutput)
        );
        generator.blockStateOutput.accept(
                MultiVariantGenerator
                        .dispatch(machine)
                        .with(BlockModelGenerators.createBooleanModelDispatch(BlockStateProperties.LIT, litModel, normalModel))
                        .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
    }
}
