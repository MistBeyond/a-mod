package com.mistbeyond.examplemod.core.energy;

import com.mistbeyond.examplemod.core.VoltageTier;
import com.mistbeyond.examplemod.core.logistic.energy.EUTransferInfo;
import com.mistbeyond.examplemod.item.ElectricItem;
import com.mistbeyond.examplemod.item.componet.ModDataComponents;
import com.mistbeyond.examplemod.util.Util;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.function.Supplier;

public class ItemAccessEUHandler implements EUEnergyHandler, EnergyConversionPermission {
    public static final Supplier<DataComponentType<Long>> ENERGY_COMPONENT = ModDataComponents.ENERGY;
    protected final ElectricItem.ChargeInfo chargeInfo;
    protected final ElectricItem validItem;
    protected final ItemAccess access;
    protected final long maxTransfer;

    ItemAccessEUHandler(ItemAccess access) {
        this(access, validateItem(access.getResource().getItem()).getElectricProperty());
    }

    ItemAccessEUHandler(ItemAccess access, ElectricItem.ChargeInfo chargeInfo) {
        this.chargeInfo = chargeInfo;
        this.access = access;
        this.validItem = validateItem(access.getResource().getItem());
        maxTransfer = chargeInfo.maxPower();
    }

    protected static ElectricItem validateItem(Item item) {
        if (!isValidItem(item)) {
            throw new IllegalArgumentException(String.format("Invalid item type: %s", item.getClass().getName()));
        }
        return (ElectricItem) item;
    }

    protected static boolean isValidItem(Item item) {
        return item instanceof ElectricItem;
    }

    @Override
    public long getEUCapacity() {
        return Util.saturatedPositiveMultiply(chargeInfo.capacity(), access.getAmount());
    }

    @Override
    public long getEUAmount() {
        return Util.saturatedPositiveMultiply(limitedRead(access.getResource()), access.getAmount());
    }

    /**
     * The ENERGY component is stored per item, shared by a whole stack, so the stored value stays integral.
     * The requested total is floored to a multiple of the stack size, and the actual inserted total is returned.
     */
    @Override
    public long insertEU(long amount, TransactionContext transaction) {
        Util.checkNonNegative(amount);
        int stackSize = access.getAmount();
        if (stackSize == 0) {
            return 0;
        }
        long amountPerItem = Math.min(maxTransfer, amount / stackSize);
        if (amountPerItem == 0) {
            return 0;
        }

        var oldResource = access.getResource();
        if (oldResource.getItem() != validItem) {
            return 0;
        }
        long oldEnergy = limitedRead(oldResource);

        long insertedPerItem = Math.min(amountPerItem, chargeInfo.capacity() - oldEnergy);
        if (insertedPerItem > 0) {
            int exchanged = access.exchange(update(oldResource, oldEnergy + insertedPerItem), stackSize, transaction);
            return insertedPerItem * exchanged;
        }
        return 0;
    }

    @Override
    public long extractEU(long amount, TransactionContext transaction) {
        Util.checkNonNegative(amount);
        int stackSize = access.getAmount();
        if (stackSize == 0) {
            return 0;
        }
        long amountPerItem = Math.min(maxTransfer, amount / stackSize);
        if (amountPerItem == 0) {
            return 0;
        }

        var oldResource = access.getResource();
        // 0 for a different item, so no extraction happens
        long oldEnergy = limitedRead(oldResource);

        long extractedPerItem = Math.min(amountPerItem, oldEnergy);
        if (extractedPerItem > 0) {
            int exchanged = access.exchange(update(oldResource, oldEnergy - extractedPerItem), stackSize, transaction);
            return extractedPerItem * exchanged;
        }
        return 0;
    }

    @Override
    public EUTransferInfo insertWith(VoltageTier voltage, long power, TransactionContext transaction) {
        Util.checkNonNegative(power);

        var inputVoltage = VoltageTier.min(chargeInfo.voltageTier(), voltage);
        var max = inputVoltage.calculatePower(chargeInfo.current());
        power = Math.min(power, max);
        return EUTransferInfo.power(inputVoltage, insertEU(power, transaction));
    }

    @Override
    public EUTransferInfo extractWith(VoltageTier voltage, long power, TransactionContext transaction) {
        Util.checkNonNegative(power);

        var outputVoltage = VoltageTier.min(chargeInfo.voltageTier(), voltage);
        var max = outputVoltage.calculatePower(chargeInfo.current());
        power = Math.min(power, max);
        return EUTransferInfo.power(outputVoltage, extractEU(power, transaction));
    }

    /**
     * Backdoor that directly sets the per-item energy. It is not transactional and must not be used inside a transaction.
     */
    @Override
    public void set(long amount) {
        var old = access.getResource();
        if (access.getAmount() == 0 || old.getItem() != validItem) {
            return;
        }
        access.exchange(update(old, amount), access.getAmount(), null);
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        Util.checkNonNegative(amount);
        if (disallowEnergyConversion()) {
            return 0;
        }
        return EUEnergyHandler.super.insert(amount, transaction);
    }

    @Override
    public VoltageTier getVoltageTier() {
        return chargeInfo.voltageTier();
    }

    protected ItemResource update(ItemResource oldResource, long energy) {
        return oldResource.with(ENERGY_COMPONENT, energy);
    }

    protected long limitedRead(ItemResource resource) {
        if (!resource.is(validItem)) {
            return 0;
        }
        return Math.clamp(resource.getOrDefault(ENERGY_COMPONENT, 0L), 0, chargeInfo.capacity());
    }
}
