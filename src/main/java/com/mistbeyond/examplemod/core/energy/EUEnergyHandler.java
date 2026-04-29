package com.mistbeyond.examplemod.core.energy;

import com.mistbeyond.examplemod.core.Values;
import com.mistbeyond.examplemod.core.VoltageTier;
import com.mistbeyond.examplemod.core.logistic.energy.EUTransferInfo;
import com.mistbeyond.examplemod.item.ElectricItem;
import com.mistbeyond.examplemod.util.EnergyUtil;
import com.mistbeyond.examplemod.util.Util;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.Objects;

/**
 * A container that stores energy in EU and participates in the full EU power grid,
 * supporting voltage/current-parameterized transfer.
 *
 * <p>Implementing this interface indicates that the block/entity can interact
 * with the EU grid using voltage and current limits (see {@link #insertWith}
 * and {@link #extractWith}). For simple FE-only compatibility, implement
 * {@link EnergyHandler} instead.
 */
public interface EUEnergyHandler extends EnergyHandler, VoltageTierLimited {
    static EUEnergyHandler forItem(ItemAccess access) {
        Objects.requireNonNull(access,
                "ItemAccess must not be null. " +
                        "Capabilities.Energy.ITEM is designed for non-null ItemAccess contexts."
        );
        var item = access.getResource().getItem();
        if (item instanceof ElectricItem electricItem) {
            var chargeInfo = electricItem.getElectricProperty();
            if (chargeInfo.isInfinite()) {
                return InfiniteEUHandler.INSTANCE;
            }
            return new ItemAccessEUHandler(access, chargeInfo);
        }
        throw new IllegalStateException("Unexpected value: " + item);
    }

    long getEUCapacity();

    long getEUAmount();

    long insertEU(long amount, TransactionContext transaction);

    long extractEU(long amount, TransactionContext transaction);

    /**
     * Pure insertion, no voltage check.
     * <p>
     * Should check {@link VoltageTierLimited#isVoltageTierSafe(VoltageTier)} or {@link VoltageTierLimited#checkVoltageTier(VoltageTier)} before insertion if exceeding the voltage limit
     * would lead to dangerous consequences, such as an explosion.
     * Even though this method performs no check, it will still limit the insertion voltage and current
     * if the input exceeds the maximum ratings.
     */
    EUTransferInfo insertWith(VoltageTier voltage, long power, TransactionContext transaction);

    /**
     * Pure insertion, no voltage check.
     * <p>
     * Should check {@link VoltageTierLimited#isVoltageTierSafe(VoltageTier)} or {@link VoltageTierLimited#checkVoltageTier(VoltageTier)} before insertion if exceeding the voltage limit
     * would lead to dangerous consequences, such as an explosion.
     * Even though this method performs no check, it will still limit the insertion voltage and current
     * if the input exceeds the maximum ratings.
     */
    default EUTransferInfo insertWith(EUTransferInfo transferInfo, TransactionContext transaction) {
        return insertWith(transferInfo.voltageTier(), transferInfo.power(), transaction);
    }

    /**
     * Pure extraction, no voltage check.
     * <p>
     * Should check {@link VoltageTierLimited#isVoltageTierSafe(VoltageTier)} or {@link VoltageTierLimited#checkVoltageTier(VoltageTier)} before extraction if exceeding the voltage limit
     * would lead to dangerous consequences, such as an explosion.
     * Even though this method performs no check, it will still limit the extraction voltage and current
     * if the input exceeds the maximum ratings.
     */
    EUTransferInfo extractWith(VoltageTier voltage, long power, TransactionContext transaction);

    /**
     * Pure extraction, no voltage check.
     * <p>
     * Should check {@link VoltageTierLimited#isVoltageTierSafe(VoltageTier)} or {@link VoltageTierLimited#checkVoltageTier(VoltageTier)} before extraction if exceeding the voltage limit
     * would lead to dangerous consequences, such as an explosion.
     * Even though this method performs no check, it will still limit the extraction voltage and current
     * if the input exceeds the maximum ratings.
     */
    default EUTransferInfo extractWith(EUTransferInfo transferInfo, TransactionContext transaction) {
        return extractWith(transferInfo.voltageTier(), transferInfo.power(), transaction);
    }

    /**
     * For special cases, directly set the amount of energy.
     */
    void set(long amount);

    /**
     * Returns the stored energy in FE.
     * Use {@link #getEUAmount()} for the EU value.
     */
    @Override
    default long getAmountAsLong() {
        return EnergyUtil.saturatingToFE(getEUAmount());
    }

    /**
     * Returns the capacity in FE.
     * Use {@link #getEUCapacity()} for the EU value.
     */
    @Override
    default long getCapacityAsLong() {
        return EnergyUtil.saturatingToFE(getEUCapacity());
    }

    /**
     * Inserts energy in FE.
     * Use {@link #insertEU(long, TransactionContext)} for the EU equivalent.
     * <p>
     * The implementor should make their own decision to allow FE insertion.
     * One of the most common cases is when {@link EnergyConversionPermission} is implemented;
     * return 0 (disallow insertion) if the insertion is {@link EnergyConversionPermission#disallowEnergyConversion() disallowed}.
     */
    @Override
    default int insert(int amount, TransactionContext transaction) {
        Util.checkNonNegative(amount);
        return Math.toIntExact(insertEU((long) amount / Values.TO_FE, transaction));
    }

    /**
     * Extracts energy in FE.
     * Use {@link #extractEU(long, TransactionContext)} for the EU equivalent.
     * <p>
     * The implementor should allow FE extraction at all time.
     */
    @Override
    default int extract(int amount, TransactionContext transaction) {
        Util.checkNonNegative(amount);
        return Math.toIntExact(extractEU((long) amount / Values.TO_FE, transaction) * Values.TO_FE);
    }
}
