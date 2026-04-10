package cn.minecraftbe.examplemod.models;

import net.minecraft.client.data.models.model.TexturedModel;

public class TexturedModels {
    public static final TexturedModel.Provider TEST_CUBE = TexturedModel.createDefault(
            TextureMappings::testCube, net.minecraft.client.data.models.model.ModelTemplates.CUBE_ALL
    );
    public static final TexturedModel.Provider SIMPLE_MACHINE = TexturedModel.createDefault(
            TextureMappings::createSimpleMachine, ModelTemplates.SIMPLE_MACHINE
    );
}
