package com.mistbeyond.examplemod.client.screen.inventory;

import com.mistbeyond.examplemod.Ids;
import com.mistbeyond.examplemod.core.registry.ProvideFactory;
import com.mistbeyond.examplemod.core.registry.RegisterContainerScreen;
import com.mistbeyond.examplemod.inventory.TestMachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

@RegisterContainerScreen(Ids.TEST_MACHINE)
public class TestMachineScreen extends AbstractContainerScreen<TestMachineMenu> {
    private static final Identifier BACKGROUND_ID = Identifier.withDefaultNamespace("textures/gui/container/furnace.png");
    private static final Identifier LIT_SPRITE = Identifier.withDefaultNamespace("container/furnace/lit_progress");
    private static final Identifier BURN_SPRITE = Identifier.withDefaultNamespace("container/furnace/burn_progress");

    public TestMachineScreen(TestMachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @ProvideFactory
    private static MenuScreens.ScreenConstructor<TestMachineMenu, TestMachineScreen> provideFactory() {
        return TestMachineScreen::new;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        int sx = leftPos, sy = topPos;

        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_ID, sx, sy, 0, 0, imageWidth, imageHeight, 256, 256);
        if (menu.isLit()) {
            if (menu.isSmelting()) {
                int smeltWidth = (int) (menu.getSmeltProgress() * 24);
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BURN_SPRITE, 24, 16, 0, 0, sx + 79, sy + 35, smeltWidth, 16);
            }
            int litHeight = (int) (menu.getLitProgress() * 14);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, LIT_SPRITE, 14, 14, 0, 14 - litHeight, sx + 57, sy + 36 + 14 - litHeight, 14, litHeight);
        }
    }
}
