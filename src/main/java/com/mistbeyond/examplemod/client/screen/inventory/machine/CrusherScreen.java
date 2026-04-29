package com.mistbeyond.examplemod.client.screen.inventory.machine;

import com.mistbeyond.examplemod.Ids;
import com.mistbeyond.examplemod.core.registry.ProvideFactory;
import com.mistbeyond.examplemod.core.registry.RegisterContainerScreen;
import com.mistbeyond.examplemod.inventory.machine.CrusherMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

@RegisterContainerScreen(Ids.CRUSHER)
public class CrusherScreen extends AbstractContainerScreen<CrusherMenu> {
    private static final Identifier BACKGROUND = Ids.thisMod("textures/gui/machine/machine.png");
    private static final Identifier ENERGY_SPRITE = Ids.thisMod("machine/energy_full");
    private static final Identifier BURN_SPRITE = Identifier.withDefaultNamespace("container/furnace/burn_progress");

    public CrusherScreen(CrusherMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @ProvideFactory
    public static MenuScreens.ScreenConstructor<CrusherMenu, CrusherScreen> provideFactory() {
        return CrusherScreen::new;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        int sx = leftPos, sy = topPos; // gui pos
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, sx, sy, 0, 0, imageWidth, imageHeight, 256, 256);
        if (menu.isWorking()) {
            int smeltWidth = (int) (menu.crushingProgress() * 24);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BURN_SPRITE, 24, 16, 0, 0, sx + 79, sy + 35, smeltWidth, 16);
        }
    }
}
