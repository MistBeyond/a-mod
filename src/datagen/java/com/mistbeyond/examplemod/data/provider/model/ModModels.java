package com.mistbeyond.examplemod.data.provider.model;

import com.mistbeyond.examplemod.Ids;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.Optional;

public class ModModels {
    // -- textured model providers --
    public static final TexturedModel.Provider TEST_CUBE = TexturedModel.createDefault(
            ModModels::testCube, ModelTemplates.CUBE_ALL
    );
    // -- templates --
    private static final ModelTemplate SIMPLE_MACHINE_TEMPLATE = new ModelTemplate(
            Optional.of(Identifier.fromNamespaceAndPath(Ids.MODID, "simple_machine").withPrefix("block/")),
            Optional.empty(),
            TextureSlot.SIDE,
            TextureSlot.FRONT
    );
    public static final TexturedModel.Provider SIMPLE_MACHINE = TexturedModel.createDefault(
            ModModels::simpleMachine, SIMPLE_MACHINE_TEMPLATE
    );

    // -- texture mappings --
    public static TextureMapping simpleMachine(Block block) {
        return new TextureMapping()
                .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(block, "_side"))
                .put(TextureSlot.FRONT, TextureMapping.getBlockTexture(block, "_front"));
    }

    public static TextureMapping testCube(Block block) {
        Material texture = TextureMapping.getBlockTexture(net.minecraft.world.level.block.Blocks.IRON_BLOCK);
        return TextureMapping.cube(texture);
    }
}
