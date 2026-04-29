package com.mistbeyond.examplemod.inventory;

import com.mistbeyond.examplemod.Ids;
import com.mistbeyond.examplemod.Init;
import com.mistbeyond.examplemod.core.registry.ProvideFactory;
import com.mistbeyond.examplemod.core.registry.RegisterMenuType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipePropertySet;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.StacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

@RegisterMenuType(Ids.TEST_MACHINE)
public class TestMachineMenu extends AbstractContainerMenu {
    public static final int INGREDIENT_SLOT = 0;
    public static final int FUEL_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;
    // INV_START = SLOT_COUNT;
    // HOTBAR_START = INV_START + 27;
    // INV_END = INV_START + 36;
    private final ContainerLevelAccess access;
    private final Level level;
    private final RecipePropertySet acceptedInputs;
    private final ContainerData containerData;

    public TestMachineMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, ContainerLevelAccess.NULL, new SimpleContainerData(4), new ItemStacksResourceHandler(3));
    }

    public TestMachineMenu(int containerId, Inventory inventory, ContainerLevelAccess access, ContainerData containerData, StacksResourceHandler<ItemStack, ItemResource> resourceHandler) {
        super(Init.REGISTRAR.menuType(Ids.TEST_MACHINE), containerId);
        checkContainerDataCount(containerData, 4);
        if (resourceHandler.size() < 3) {
            throw new IllegalArgumentException("Container size " + resourceHandler.size() + " is smaller than expected 3");
        }
        this.access = access;
        this.containerData = containerData;
        this.level = inventory.player.level();
        this.acceptedInputs = this.level.recipeAccess().propertySet(RecipePropertySet.FURNACE_INPUT);
        this.addSlot(new ResourceHandlerSlot(resourceHandler, resourceHandler::set, INGREDIENT_SLOT, 56, 17));
        this.addSlot(new ResourceHandlerSlot(resourceHandler, resourceHandler::set, FUEL_SLOT, 56, 53));
        this.addSlot(new ResourceHandlerSlot(resourceHandler, resourceHandler::set, OUTPUT_SLOT, 116, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        this.addStandardInventorySlots(inventory, 8, 84);
        this.addDataSlots(containerData);
    }

    @ProvideFactory
    private static MenuType.MenuSupplier<?> provideFactory() {
        return TestMachineMenu::new;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        var snapshotStack = ItemStack.EMPTY;
        var slot = this.slots.get(slotIndex);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        var stack = slot.getItem();
        snapshotStack = stack.copy();

        if (slotIndex == 2) {
            if (!this.moveItemStackTo(stack, 3, 39, false)) {
                return ItemStack.EMPTY;
            }

            slot.onQuickCraft(stack, snapshotStack); // collect experience
        } else if (slotIndex >= 3 && slotIndex < 39) {
            if (this.canSmelt(stack)) {
                if (!this.moveItemStackTo(stack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.isFuel(stack)) {
                if (!this.moveItemStackTo(stack, 1, 2, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotIndex < 30) {
                if (!this.moveItemStackTo(stack, 30, 39, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 3, 30, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 3, 39, false)) {
            // input or fuel slot
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == snapshotStack.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return snapshotStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(access, player, Init.REGISTRAR.block(Ids.TEST_MACHINE));
    }

    public boolean isLit() {
        return containerData.get(0) > 0;
    }

    public boolean isSmelting() {
        return containerData.get(3) > 0;
    }

    public double getLitProgress() {
        return isLit() ? (double) (containerData.get(0)) / containerData.get(1) : 0;
    }

    public double getSmeltProgress() {
        return isSmelting() ? (double) containerData.get(2) / containerData.get(3) : 0;
    }

    protected boolean canSmelt(ItemStack itemStack) {
        return this.acceptedInputs.test(itemStack);
    }

    protected boolean isFuel(ItemStack itemStack) {
        return itemStack.getBurnTime(RecipeType.SMELTING, this.level.fuelValues()) > 0;
    }
}
