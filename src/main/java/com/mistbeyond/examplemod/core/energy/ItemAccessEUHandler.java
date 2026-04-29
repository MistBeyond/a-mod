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

    private static long min(long a, long b, long c) {
        return Math.min(Math.min(a, b), c);
    }

    @Override
    public long getEUCapacity() {
        return chargeInfo.capacity();
    }

    @Override
    public long getEUAmount() {
        return limitedRead(access.getResource());
    }

    @Override
    public long insertEU(long amount, TransactionContext transaction) {
        Util.checkNonNegative(amount);
        var oldResource = access.getResource();
        if (access.getAmount() == 0) {
            return 0;
        }
        if (access.getAmount() != 1) {
            throw new UnsupportedOperationException(String.format("Invalid item amount for EU transfer: %s", access.getAmount()));
        }
        if (oldResource.getItem() != validItem) {
            return 0;
        }

        long oldEnergy = limitedRead(oldResource);
        long inserted = min(chargeInfo.capacity() - oldEnergy, maxTransfer, amount);
        if (inserted > 0) {
            access.exchange(update(oldResource, oldEnergy + inserted), access.getAmount(), transaction);
            return inserted;
        }
        return 0;
    }

    @Override
    public long extractEU(long amount, TransactionContext transaction) {
        Util.checkNonNegative(amount);
        var oldResource = access.getResource();
        if (access.getAmount() == 0) {
            return 0;
        }
        if (access.getAmount() != 1) {
            throw new UnsupportedOperationException(String.format("Invalid item amount for EU transfer: %s", access.getAmount()));
        }
        if (oldResource.getItem() != validItem) {
            return 0;
        }

        long oldEnergy = limitedRead(oldResource);
        long extracted = min(oldEnergy, maxTransfer, amount);
        if (extracted > 0) {
            access.exchange(update(oldResource, oldEnergy - extracted), access.getAmount(), transaction);
            return extracted;
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

    @Override
    public void set(long amount) {
        if (access.getAmount() == 0) {
            return;
        }
        if (access.getAmount() != 1) {
            throw new UnsupportedOperationException(String.format("Invalid item amount for EU transfer: %s", access.getAmount()));
        }
        var old = access.getResource();
        if (old.getItem() != validItem) {
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
