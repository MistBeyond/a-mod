package cn.minecraftbe.examplemod.inventory;

import cn.minecraftbe.examplemod.block.Blocks;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;

public abstract class AbstractSingleBlockMachineMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;

    protected AbstractSingleBlockMachineMenu(MenuType<?> menuType, int containerId, ContainerLevelAccess access) {
        super(menuType, containerId);
        this.access = access;
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(this.access, player, Blocks.TEST_MACHINE.get());
    }
}
