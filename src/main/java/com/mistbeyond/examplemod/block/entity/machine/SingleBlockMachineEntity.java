package com.mistbeyond.examplemod.block.entity.machine;

import com.mistbeyond.examplemod.block.machine.SingleBlockMachine;
import com.mistbeyond.examplemod.core.energy.CurrentLimited;
import com.mistbeyond.examplemod.core.energy.EnergyConversionPermission;
import com.mistbeyond.examplemod.core.energy.VoltageTierLimited;
import com.mistbeyond.examplemod.recipe.ElectricRecipe;
import com.mistbeyond.examplemod.util.RecipeUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public abstract class SingleBlockMachineEntity<R extends ElectricRecipe<?>> extends BlockEntity implements
        MenuProvider, EnergyConversionPermission, VoltageTierLimited, CurrentLimited, ElectricMachine {
    protected @Nullable RecipeHolder<R> usingRecipe = null;
    private @Nullable ResourceKey<Recipe<?>> loadingKey = null;

    public SingleBlockMachineEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) {
        super(type, worldPosition, blockState);
    }

    public static <T extends SingleBlockMachineEntity<?>> void serverTick(ServerLevel serverLevel, BlockPos blockPos, BlockState state, T entity) {
        boolean changed = false;
        try (Transaction transaction = Transaction.openRoot()) {
            entity.charge(serverLevel, transaction);
            transaction.commit();
        }
        try (var transaction = Transaction.openRoot()) {
            entity.updateSnapshot(transaction);
            if (entity.usingRecipe == null) {
                entity.setUsingRecipe(serverLevel, transaction);
            }
            if (entity.usingRecipe != null) {
                if (entity.processRecipe(serverLevel, transaction)) {
                    changed = true;
                    transaction.commit();
                }
            }
        }
        final boolean isWorking = entity.isWorking();
        if (isWorking != state.getValue(SingleBlockMachine.WORKING)) {
            serverLevel.setBlock(blockPos, state.setValue(SingleBlockMachine.WORKING, isWorking), Block.UPDATE_ALL);
            changed = true;
        }
        if (changed) {
            setChanged(serverLevel, blockPos, state);
        }
    }

    public abstract boolean processRecipe(ServerLevel serverLevel, TransactionContext transaction);

    public abstract boolean isWorking();

    @Override
    public void onLoad() {
        super.onLoad();
        initUsingRecipe();
    }

    /**
     * Subclasses only need to implement the logic for setting {@link SingleBlockMachineEntity#usingRecipe}.
     * <b>Do not</b> check if {@link SingleBlockMachineEntity#usingRecipe} is null when implementing this method.
     */
    protected abstract void setUsingRecipe(ServerLevel serverLevel, TransactionContext transaction);

    /**
     * Subclasses need to capture their own transient states (e.g. progress) into a snapshot here that can be rolled back if processing failed.
     * For handlers, their snapshot should be managed by themselves when transferring, so do not update their snapshot here.
     */
    protected abstract void updateSnapshot(TransactionContext transaction);

    /**
     * Subclasses only need to load other data.
     * Data of handlers (energy, item, upgrade and fluid) and the using recipe have been deserialized in {@link SingleBlockMachineEntity}, so it's important to call super.
     */
    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        if (getEnergyHandler() instanceof ValueIOSerializable serializable) {
            serializable.deserialize(input);
        }
        if (this instanceof CanUpgrade machine) {
            machine.getUpgradeHandler().deserialize(input);
        }
        if (this instanceof CanProcessItem machine) {
            machine.getItemHandler().deserialize(input);
        }
        if (this instanceof CanProcessFluid machine) {
            machine.getFluidHandler().deserialize(input);
        }
        loadingKey = input.read("using_recipe", Recipe.KEY_CODEC).orElse(null);
    }

    /**
     * Subclasses only need to save other data.
     * Data of handlers (energy, item, upgrade and fluid) and the using recipe have been serialized in {@link SingleBlockMachineEntity}, so it's important to call super.
     */
    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (getEnergyHandler() instanceof ValueIOSerializable serializable) {
            serializable.serialize(output);
        }
        if (this instanceof CanUpgrade machine) {
            machine.getUpgradeHandler().serialize(output);
        }
        if (this instanceof CanProcessItem machine) {
            machine.getItemHandler().serialize(output);
        }
        if (this instanceof CanProcessFluid machine) {
            machine.getFluidHandler().serialize(output);
        }
        if (usingRecipe != null) {
            output.store("using_recipe", Recipe.KEY_CODEC, usingRecipe.id());
        } else {
            output.discard("using_recipe");
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && this instanceof CanProcessItem machine) {
            Containers.dropContents(level, pos, machine.getItemHandler().copyToList());
        }
    }

    private void initUsingRecipe() {
        if (usingRecipe == null && loadingKey != null) {
            Objects.requireNonNull(level);
            @SuppressWarnings("unchecked")
            var recipe = (RecipeHolder<R>) RecipeUtil.byKey(loadingKey, level);
            usingRecipe = recipe;
            loadingKey = null;
        }
    }
}
