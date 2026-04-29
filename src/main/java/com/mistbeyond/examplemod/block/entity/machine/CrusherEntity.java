package com.mistbeyond.examplemod.block.entity.machine;

import com.mistbeyond.examplemod.Ids;
import com.mistbeyond.examplemod.Init;
import com.mistbeyond.examplemod.core.VoltageTier;
import com.mistbeyond.examplemod.core.energy.EUEnergyHandler;
import com.mistbeyond.examplemod.core.energy.SimpleEUHandler;
import com.mistbeyond.examplemod.core.logistic.energy.EUTransferInfo;
import com.mistbeyond.examplemod.core.registry.ProvideFactory;
import com.mistbeyond.examplemod.core.registry.RegisterBlockEntityType;
import com.mistbeyond.examplemod.inventory.machine.CrusherMenu;
import com.mistbeyond.examplemod.recipe.CrushingRecipe;
import com.mistbeyond.examplemod.recipe.RecipeTypes;
import com.mistbeyond.examplemod.util.EnergyUtil;
import com.mistbeyond.examplemod.util.ItemUtil;
import com.mistbeyond.examplemod.util.transfer.StacksHandlerUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

@Slf4j
@RegisterBlockEntityType(Ids.CRUSHER)
public class CrusherEntity extends SingleBlockMachineEntity<CrushingRecipe> implements CanProcessItem {
    private static final int MAX_CURRENT = 2;
    private static final VoltageTier VOLTAGE_TIER = VoltageTier.LOW;
    private final EUEnergyHandler energyHandler = new SimpleEUHandler(VOLTAGE_TIER);
    private final RecipeManager.CachedCheck<SingleRecipeInput, CrushingRecipe> quickCheck = RecipeManager.createCheck(RecipeTypes.CRUSHING.get());
    @Getter
    private final InnerItemHandler itemHandler = new InnerItemHandler(3);
    private final Journal journal = new Journal();
    private int progress;
    public final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int dataId) {
            return switch (dataId) {
                case 0 -> progress;
                case 1 -> usingRecipe == null ? 0 : usingRecipe.value().getProcessInfo().duration();
                default -> throw new IllegalStateException("Unexpected value: " + dataId);
            };
        }

        @Override
        public void set(int dataId, int value) {
            switch (dataId) {
                case 0 -> progress = value;
                case 1 -> throw new UnsupportedOperationException("Recipe processing time is read only");
                default -> throw new IllegalStateException("Unexpected value: " + dataId);
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };
    private boolean isWorking = false;
    private boolean itemConsumed = false;

    public CrusherEntity(BlockPos worldPosition, BlockState blockState) {
        super(Init.REGISTRAR.blockEntityType(Ids.CRUSHER), worldPosition, blockState);
    }

    @ProvideFactory
    private static BlockEntityType.BlockEntitySupplier<?> provideFactory() {
        return CrusherEntity::new;
    }

    public float crushingProgress() {
        if (usingRecipe != null) {
            var total = usingRecipe.value().getProcessInfo().duration();
            if (total == 0) {
                return 0;
            }
            return (float) progress / total;
        }
        return 0;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.examplemod.crusher");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new CrusherMenu(containerId, inventory, ContainerLevelAccess.create(player.level(), worldPosition), itemHandler, dataAccess);
    }

    @Override
    public boolean processRecipe(ServerLevel serverLevel, TransactionContext transaction) {
        Objects.requireNonNull(usingRecipe);
        // preparation
        var input = new SingleRecipeInput(StacksHandlerUtil.getItem(itemHandler, 0));
        var recipe = usingRecipe.value();

        // processing
        var processInfo = recipe.getProcessInfo();
        if (processInfo.power() != energyHandler.extractEU(processInfo.power(), transaction)) {
            return false;
        }
        if (!itemConsumed) {
            itemConsumed = true;
            StacksHandlerUtil.shrink(itemHandler, 0, 1);
        }

        progress++;
        isWorking = true;

        // on finishing
        if (progress == processInfo.duration()) {
            var result = recipe.assemble(input);
            var expected = result.count();
            if (itemHandler.insertInternal(2, ItemResource.of(result), expected, transaction) != expected) {
                return false;
            }
            itemConsumed = false;
            usingRecipe = null;
            progress = 0;
        }
        return true;
    }

    @Override
    public boolean isWorking() {
        return isWorking;
    }

    @Override
    protected void setUsingRecipe(ServerLevel serverLevel, TransactionContext transaction) {
        isWorking = false;
        SingleRecipeInput input = new SingleRecipeInput(StacksHandlerUtil.getItem(itemHandler, 0));

        var optionalHolder = quickCheck.getRecipeFor(input, serverLevel);
        if (optionalHolder.isEmpty()) {
            return;
        }
        var recipe = optionalHolder.get().value();
        var energyInfo = recipe.getProcessInfo();
        if (!isVoltageTierSafe(energyInfo.voltageTier())) {
            return;
        }
        try (Transaction sim = Transaction.open(transaction)) {
            if (energyHandler.extractEU(energyInfo.power(), sim) != energyInfo.power()) {
                return;
            }
            var result = recipe.assemble(input);
            if (result.count() != itemHandler.insertInternal(2, ItemResource.of(result), result.count(), sim)) {
                return;
            }
        }
        usingRecipe = optionalHolder.get();
    }

    @Override
    protected void updateSnapshot(TransactionContext transaction) {
        journal.updateSnapshots(transaction);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        itemConsumed = input.getBooleanOr("item_consumed", false);
        progress = input.getIntOr("progress", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("item_consumed", itemConsumed);
        output.putInt("progress", progress);
    }

    @Override
    public EUEnergyHandler getEnergyHandler() {
        return energyHandler;
    }

    @Override
    public EUTransferInfo charge(ServerLevel serverLevel, TransactionContext transaction) {
        var battery = StacksHandlerUtil
                .getItem(itemHandler, 1)
                .getCapability(Capabilities.Energy.ITEM, ItemAccess.forHandlerIndexStrict(itemHandler, 1));
        if (battery == null) {
            return EUTransferInfo.ZERO;
        }
        long request = energyHandler.getEUCapacity() - energyHandler.getEUAmount();
        if (battery instanceof EUEnergyHandler euBattery) {
            if (euBattery.isVoltageTierSafe(energyHandler.getVoltageTier())) {
                return energyHandler.insertWith(euBattery.extractWith(energyHandler.getVoltageTier(), request, transaction), transaction);
            }
        }
        if (allowEnergyConversion()) {
            var requestFE = EnergyUtil.saturatingToFE(request);
            try (Transaction insertFE = Transaction.open(transaction)) {
                int actual = battery.extract(EnergyUtil.saturatingToIntFE(requestFE), insertFE);
                if (energyHandler.insert(actual, insertFE) == actual) {
                    insertFE.commit();
                    return EUTransferInfo.power(energyHandler.getVoltageTier(), EnergyUtil.toEU(actual));
                }
            }
        }
        return EUTransferInfo.ZERO;
    }

    @Override
    public VoltageTier getVoltageTier() {
        return VOLTAGE_TIER;
    }

    @Override
    public long getMaxOutputCurrent() {
        return MAX_CURRENT;
    }

    @Override
    public long getMaxInputCurrent() {
        return MAX_CURRENT;
    }

    private record State(int remaining, @Nullable RecipeHolder<CrushingRecipe> recipe, boolean itemConsumed) {
    }

    private class Journal extends SnapshotJournal<State> {
        @Override
        protected State createSnapshot() {
            return new State(progress, usingRecipe, itemConsumed);
        }

        @Override
        protected void revertToSnapshot(State snapshot) {
            if (snapshot.remaining != progress) {
                progress = snapshot.remaining;
            }
            if (snapshot.recipe != usingRecipe) {
                usingRecipe = snapshot.recipe;
            }
            if (snapshot.itemConsumed != itemConsumed) {
                itemConsumed = snapshot.itemConsumed;
            }
        }
    }

    private class InnerItemHandler extends ItemStacksResourceHandler {
        private boolean isInternalInsertion = false;

        public InnerItemHandler(int size) {
            super(size);
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return switch (index) {
                case 0 -> true;
                case 1 -> ItemUtil.isBatteryLike(resource.toStack(), allowEnergyConversion());
                case 2 -> isInternalInsertion;
                default -> throw new IllegalStateException("Unexpected value: " + index);
            };
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            setChanged();
        }

        private int insertInternal(int index, ItemResource resource, int amount, TransactionContext transaction) {
            isInternalInsertion = true;
            int ret = insert(index, resource, amount, transaction);
            isInternalInsertion = false;
            return ret;
        }
    }
}
