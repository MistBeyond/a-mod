package cn.minecraftbe.examplemod.models;

import cn.minecraftbe.examplemod.ExampleMod;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public class ModelTemplates {
    public static final ModelTemplate SIMPLE_MACHINE = new ModelTemplate(
            Optional.of(Identifier.fromNamespaceAndPath(ExampleMod.MODID, "simple_machine").withPrefix("block/")),
            Optional.empty(),
            TextureSlot.SIDE,
            TextureSlot.FRONT
    );
}
