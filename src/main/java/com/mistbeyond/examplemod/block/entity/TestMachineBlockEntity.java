package com.mistbeyond.examplemod.block.entity;

import com.mistbeyond.examplemod.Ids;
import com.mistbeyond.examplemod.Init;
import com.mistbeyond.examplemod.block.TestMachine;
import com.mistbeyond.examplemod.core.registry.ProvideFactory;
import com.mistbeyond.examplemod.core.registry.RegisterBlockEntityType;
import com.mistbeyond.examplemod.inventory.TestMachineMenu;
import com.mistbeyond.examplemod.util.transfer.StacksHandlerUtil;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.jspecify.annotations.Nullable;

@RegisterBlockEntityType(Ids.TEST_MACHINE)
public class TestMachineBlockEntity extends BlockEntity implements MenuProvider {
    public static final int INGREDIENT_SLOT = 0;
    public static final int FUEL_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;
    public static final int SMELT_SPEED = 2;
    private static final Component DEFAULT_NAME = Component.translatable("block.examplemod.test_machine");
    private static final RecipeManager.CachedCheck<SingleRecipeInput, SmeltingRecipe> QUICK_CHECK = RecipeManager.createCheck(RecipeType.SMELTING);
    @Getter
    private final ItemStacksResourceHandler itemHandler = new ItemStacksResourceHandler(3) {
        @Override
        public boolean isValid(int index, ItemResource resource) {
            return switch (index) {
                case 0, 1 -> true;
                case 2 -> false;
                default -> throw new IllegalStateException("Unexpected value: " + index);
            };
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            setChanged();
        }
    };
    private int litRemaining;
    private int litTotal;
    private int smeltProgress;
    private int smeltTotal;
    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int dataId) {
            return switch (dataId) {
                case 0 -> litRemaining;
                case 1 -> litTotal;
                case 2 -> smeltProgress;
                case 3 -> smeltTotal;
                default -> throw new IllegalStateException("Unexpected value: " + dataId);
            };
        }

        @Override
        public void set(int dataId, int value) {
            switch (dataId) {
                case 0 -> litRemaining = value;
                case 1 -> litTotal = value;
                case 2 -> smeltProgress = value;
                case 3 -> smeltTotal = value;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public TestMachineBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(Init.REGISTRAR.blockEntityType(Ids.TEST_MACHINE), worldPosition, blockState);
    }

    public static void serverTick(ServerLevel serverLevel, BlockPos pos, BlockState state, TestMachineBlockEntity entity) {
        // state before performing
        final boolean wasLit = entity.litRemaining > 0;
        // state after performing
        final boolean isLit;
        final boolean shouldConsumeFuel;
        final boolean shouldComplete;
        entity.smeltTotal = entity.getCookingTime(serverLevel);

        // perform, it's time-sensitive
        boolean changed;
        if (wasLit) {
            entity.litRemaining--;
            if (entity.canSmelt(serverLevel)) {
                entity.smeltProgress++;
            } else {
                entity.resetSmelting();
            }
        } else {
            if (entity.smeltProgress > 0) entity.smeltProgress -= 5;
            if (entity.smeltProgress < 0) entity.smeltProgress = 0;
        }

        // get the state after performing
        if (!entity.isLit()) {
            shouldConsumeFuel = entity.canConsumeFuel(serverLevel);
            isLit = shouldConsumeFuel;
        } else {
            shouldConsumeFuel = false;
            isLit = true;
        }
        shouldComplete = entity.smeltTotal != 0 && entity.smeltProgress >= entity.smeltTotal;

        // finalize, or non-time-sensitive perform
        if (shouldConsumeFuel) {
            // actually, it's a time-sensitive action that must be performed after decreasing remaining lit time
            entity.consumeFuel(serverLevel);
        }
        if (shouldComplete) {
            entity.finishSmelting(serverLevel);
        }
        if (!isLit) {
            entity.litTotal = 0;
        }
        changed = shouldConsumeFuel || isLit || shouldComplete;
        if (isLit != wasLit) {
            serverLevel.setBlock(pos, state.setValue(TestMachine.LIT, isLit), Block.UPDATE_ALL);
            changed = true;
        }
        if (changed) {
            setChanged(serverLevel, pos, state);
        }
    }

    @ProvideFactory
    private static BlockEntityType.BlockEntitySupplier<?> provideFactory() {
        return TestMachineBlockEntity::new;
    }

    public boolean isLit() {
        return litRemaining > 0;
    }

    public double getLitProgress() {
        return isLit() ? (double) litRemaining / litTotal : 0;
    }

    public double getSmeltProgress() {
        return smeltTotal > 0 ? (double) smeltProgress / smeltTotal : 0;
    }

    public int getCookingTime(ServerLevel serverLevel) {
        var ingredient = StacksHandlerUtil.getItem(itemHandler, INGREDIENT_SLOT);
        if (ingredient.isEmpty()) {
            return 0;
        }

        var input = new SingleRecipeInput(ingredient);
        var recipe = QUICK_CHECK.getRecipeFor(input, serverLevel).orElse(null);
        if (recipe == null) {
            return 0;
        }
        return recipe.value().cookingTime() / SMELT_SPEED;
    }

    public boolean canSmelt(ServerLevel serverLevel) {
        var ingredient = StacksHandlerUtil.getItem(itemHandler, INGREDIENT_SLOT);
        var output = StacksHandlerUtil.getItem(itemHandler, OUTPUT_SLOT);
        if (ingredient.isEmpty()) {
            return false;
        }
        var input = new SingleRecipeInput(ingredient);
        var recipe = QUICK_CHECK.getRecipeFor(input, serverLevel).orElse(null);
        if (recipe == null) {
            return false;
        }
        var result = recipe.value().assemble(input);
        var mergeable = result.getItem() == output.getItem() && output.count() + result.count() <= output.getMaxStackSize();
        return output.isEmpty() || mergeable;
    }

    public boolean canConsumeFuel(ServerLevel serverLevel) {
        var fuel = StacksHandlerUtil.getItem(itemHandler, FUEL_SLOT);
        if (fuel.isEmpty()) {
            return false;
        }
        if (fuel.getBurnTime(RecipeType.SMELTING, serverLevel.fuelValues()) < 0) {
            return false;
        }
        return canSmelt(serverLevel);
    }

    public void consumeFuel(ServerLevel serverLevel) {
        var fuel = StacksHandlerUtil.getItem(itemHandler, FUEL_SLOT);
        litTotal = litRemaining = fuel.getBurnTime(RecipeType.SMELTING, serverLevel.fuelValues()) / SMELT_SPEED;
        StacksHandlerUtil.shrink(itemHandler, FUEL_SLOT, 1);
    }

    public void resetSmelting() {
        this.smeltTotal = 0;
        this.smeltProgress = 0;
    }

    public void finishSmelting(ServerLevel serverLevel) {
        var input = new SingleRecipeInput(StacksHandlerUtil.getItem(itemHandler, INGREDIENT_SLOT));
        var recipe = QUICK_CHECK.getRecipeFor(input, serverLevel).orElse(null);
        if (recipe == null) {
            return;
        }
        var result = recipe.value().assemble(input);
        StacksHandlerUtil.replaceAndGrow(itemHandler, OUTPUT_SLOT, result.getItem(), result.count());
        StacksHandlerUtil.shrink(itemHandler, INGREDIENT_SLOT, 1);
        resetSmelting();
    }

    @Override
    public Component getDisplayName() {
        return DEFAULT_NAME;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new TestMachineMenu(containerId, inventory, ContainerLevelAccess.create(player.level(), this.worldPosition), dataAccess, itemHandler);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        itemHandler.deserialize(input);
        this.litRemaining = input.getIntOr("lit_remaining_time", 0);
        this.litTotal = input.getIntOr("lit_total_time", 0);
        this.smeltProgress = input.getIntOr("smelt_progress", 0);
        this.smeltTotal = input.getIntOr("smelt_total_time", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        itemHandler.serialize(output);
        output.putInt("lit_remaining_time", this.litRemaining);
        output.putInt("lit_total_time", this.litTotal);
        output.putInt("smelt_progress", this.smeltProgress);
        output.putInt("smelt_total_time", this.smeltTotal);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null) {
            Containers.dropContents(level, pos, itemHandler.copyToList());
        }
    }
}
