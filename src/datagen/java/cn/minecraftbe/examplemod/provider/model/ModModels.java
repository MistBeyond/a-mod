package cn.minecraftbe.examplemod.provider.model;

import cn.minecraftbe.examplemod.ExampleMod;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.Optional;

public class ModModels {
    // -- templates --
    private static final ModelTemplate SIMPLE_MACHINE_TEMPLATE = new ModelTemplate(
            Optional.of(Identifier.fromNamespaceAndPath(ExampleMod.MODID, "simple_machine").withPrefix("block/")),
            Optional.empty(),
            TextureSlot.SIDE,
            TextureSlot.FRONT
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

    // -- textured model providers --
    public static final TexturedModel.Provider TEST_CUBE = TexturedModel.createDefault(
            ModModels::testCube, ModelTemplates.CUBE_ALL
    );
    public static final TexturedModel.Provider SIMPLE_MACHINE = TexturedModel.createDefault(
            ModModels::simpleMachine, SIMPLE_MACHINE_TEMPLATE
    );
}
