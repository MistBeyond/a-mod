package com.mistbeyond.examplemod.inventory.machine;

import com.mistbeyond.examplemod.Ids;
import com.mistbeyond.examplemod.Init;
import com.mistbeyond.examplemod.core.registry.ProvideFactory;
import com.mistbeyond.examplemod.core.registry.RegisterMenuType;
import com.mistbeyond.examplemod.inventory.BatterySlot;
import com.mistbeyond.examplemod.recipe.RecipeTypes;
import com.mistbeyond.examplemod.util.RecipeUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.StacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.jetbrains.annotations.Range;

@RegisterMenuType(Ids.CRUSHER)
public class CrusherMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;
    private final Inventory inventory;
    private final ContainerData dataAccess;

    public CrusherMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, ContainerLevelAccess.NULL, new ItemStacksResourceHandler(3), new SimpleContainerData(2));
    }

    public CrusherMenu(int containerId, Inventory inventory, ContainerLevelAccess access, StacksResourceHandler<ItemStack, ItemResource> itemHandler, ContainerData dataAccess) {
        super(Init.REGISTRAR.menuType(Ids.CRUSHER), containerId);
        this.access = access;
        this.inventory = inventory;
        this.dataAccess = dataAccess;
        checkContainerDataCount(dataAccess, 2);
        addDataSlots(this.dataAccess);
        addSlot(new ResourceHandlerSlot(itemHandler, itemHandler::set, 0, 56, 17));
        addSlot(new BatterySlot(itemHandler, itemHandler::set, 1, 56, 53));
        addSlot(new ResourceHandlerSlot(itemHandler, itemHandler::set, 2, 116, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        addStandardInventorySlots(inventory, 8, 84);
    }

    @ProvideFactory
    private static MenuType.MenuSupplier<?> provideFactory() {
        return CrusherMenu::new;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        var slot = this.getSlot(slotIndex);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        var snapshot = slot.getItem();
        var stack = snapshot.copy();

        if (slotIndex < 3) {
            if (!this.moveItemStackTo(stack, 3, 39, false)) {
                return ItemStack.EMPTY;
            }
        } else if (slotIndex < 39) {
            if (isCrushable(stack)) {
                if (!this.moveItemStackTo(stack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotIndex < 30) {
                if (!this.moveItemStackTo(stack, 30, 39, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(stack, 3, 30, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.count() == snapshot.count()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return snapshot;
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(access, player, Init.REGISTRAR.block(Ids.CRUSHER));
    }

    public @Range(from = 0, to = 1) float crushingProgress() {
        var total = dataAccess.get(1);
        if (total == 0) {
            return 0;
        }
        return (float) dataAccess.get(0) / total;
    }

    public boolean isWorking() {
        return dataAccess.get(0) > 0;
    }

    protected boolean isCrushable(ItemStack stack) {
        return RecipeUtil.isInputIn(this.inventory.player.level(), RecipeTypes.CRUSHING, stack);
    }
}
