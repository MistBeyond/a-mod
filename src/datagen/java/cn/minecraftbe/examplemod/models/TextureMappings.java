package cn.minecraftbe.examplemod.models;

import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.world.level.block.Block;

public class TextureMappings {
    public static TextureMapping createSimpleMachine(Block block) {
        return new TextureMapping()
                .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(block, "_side"))
                .put(TextureSlot.FRONT, TextureMapping.getBlockTexture(block, "_front"));
    }

    public static TextureMapping testCube(Block block) {
        Material texture = TextureMapping.getBlockTexture(net.minecraft.world.level.block.Blocks.IRON_BLOCK);
        return TextureMapping.cube(texture);
    }
}
