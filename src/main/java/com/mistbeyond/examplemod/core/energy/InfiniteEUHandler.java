package com.mistbeyond.examplemod.core.energy;

import com.mistbeyond.examplemod.core.VoltageTier;
import com.mistbeyond.examplemod.core.logistic.energy.EUTransferInfo;
import com.mistbeyond.examplemod.util.Util;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class InfiniteEUHandler implements EUEnergyHandler, EnergyConversionPermission {
    public static final InfiniteEUHandler INSTANCE = new InfiniteEUHandler();
    private static final VoltageTier VOLTAGE = VoltageTier.MAX;

    private InfiniteEUHandler() {
    }

    @Override
    public long getEUCapacity() {
        return Long.MAX_VALUE;
    }

    @Override
    public long getEUAmount() {
        return Long.MAX_VALUE;
    }

    @Override
    public long insertEU(long amount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public long extractEU(long amount, TransactionContext transaction) {
        Util.checkNonNegative(amount);
        return amount;
    }

    @Override
    public EUTransferInfo insertWith(VoltageTier voltageTier, long power, TransactionContext transaction) {
        return EUTransferInfo.ZERO;
    }

    @Override
    public EUTransferInfo extractWith(VoltageTier voltageTier, long power, TransactionContext transaction) {
        Util.checkNonNegative(power);
        return EUTransferInfo.power(voltageTier, power);
    }

    @Override
    public void set(long amount) {
    }

    @Override
    public long getAmountAsLong() {
        return Long.MAX_VALUE;
    }

    @Override
    public long getCapacityAsLong() {
        return Long.MAX_VALUE;
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        Util.checkNonNegative(amount);
        return amount;
    }

    @Override
    public VoltageTier getVoltageTier() {
        return VOLTAGE;
    }

    @Override
    public boolean isVoltageTierSafe(VoltageTier other) {
        return true;
    }
}
