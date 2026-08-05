package com.mistbeyond.examplemod.data.model;

import com.mistbeyond.examplemod.Ids;
import com.mistbeyond.examplemod.block.IConnectableBlock;
import com.mistbeyond.examplemod.block.state.StateProperties;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.ConditionBuilder;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import static net.minecraft.world.level.block.Blocks.BLAST_FURNACE;
import static net.minecraft.world.level.block.Blocks.FURNACE;

public class ExampleModModelGenerators {
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
        Material frontTextureOn = TextureMapping.getBlockTexture(FURNACE, "_front_on");
        Material frontTextureOff = TextureMapping.getBlockTexture(FURNACE, "_front");
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
                        .with(BlockModelGenerators.createBooleanModelDispatch(StateProperties.WORKING, litModel, normalModel))
                        .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
    }

    public static void createTestMachine(Block machine, TexturedModel.Provider provider, BlockModelGenerators generator) {
        Material frontTextureOn = TextureMapping.getBlockTexture(BLAST_FURNACE, "_front_on");
        Material frontTextureOff = TextureMapping.getBlockTexture(BLAST_FURNACE, "_front");
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

    public static void createWire(Block wire, BlockModelGenerators generator) {
        MultiPartGenerator multipart = MultiPartGenerator.multiPart(wire)
                .with(BlockModelGenerators.plainVariant(wireModel("wire")));
        multipart = withWirePart(multipart, IConnectableBlock.NORTH, "wire_north");
        multipart = withWirePart(multipart, IConnectableBlock.SOUTH, "wire_south");
        multipart = withWirePart(multipart, IConnectableBlock.EAST, "wire_east");
        multipart = withWirePart(multipart, IConnectableBlock.WEST, "wire_west");
        multipart = withWirePart(multipart, IConnectableBlock.UP, "wire_up");
        multipart = withWirePart(multipart, IConnectableBlock.DOWN, "wire_down");
        generator.blockStateOutput.accept(multipart);
    }

    public static void createWireItem(Item item, ItemModelGenerators generator) {
        generator.itemModelOutput.accept(
                item,
                ItemModelUtils.plainModel(Identifier.fromNamespaceAndPath(Ids.MODID, "item/wire"))
        );
    }

    private static MultiPartGenerator withWirePart(MultiPartGenerator multipart, BooleanProperty property, String model) {
        return multipart.with(
                new ConditionBuilder().term(property, true),
                BlockModelGenerators.plainVariant(wireModel(model))
        );
    }

    private static Identifier wireModel(String path) {
        return Identifier.fromNamespaceAndPath(Ids.MODID, "block/" + path);
    }
}
