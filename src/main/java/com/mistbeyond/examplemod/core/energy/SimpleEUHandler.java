package com.mistbeyond.examplemod.core.energy;

import com.mistbeyond.examplemod.core.Values;
import com.mistbeyond.examplemod.core.VoltageTier;
import com.mistbeyond.examplemod.core.logistic.energy.EUTransferInfo;
import com.mistbeyond.examplemod.util.Util;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class SimpleEUHandler implements EUEnergyHandler, EnergyConversionPermission, ValueIOSerializable {
    private final long maxCurrentInput, maxCurrentOutput;
    private final EUJournal journal = new EUJournal();
    private final long maxInsert, maxExtract;
    private final VoltageTier voltageTier;
    private final long capacity;
    private long energy;

    public SimpleEUHandler(VoltageTier voltageTier) {
        this(voltageTier, Values.DEFAULT_IO_CURRENT);
    }

    public SimpleEUHandler(VoltageTier voltageTier, long currentLimit) {
        this(voltageTier, currentLimit, currentLimit);
    }

    public SimpleEUHandler(VoltageTier voltageTier, long maxCurrentInput, long maxCurrentOutput) {
        this(voltageTier, maxCurrentInput, maxCurrentOutput, Values.calculateCapacity(voltageTier.value, maxCurrentInput), 0);
    }

    public SimpleEUHandler(VoltageTier voltageTier, long maxCurrentInput, long maxCurrentOutput, long capacity, long energy) {
        Util.checkNonNegative(maxCurrentInput);
        Util.checkNonNegative(maxCurrentOutput);
        Util.checkNonNegative(capacity);
        Util.checkNonNegative(energy);
        this.maxCurrentInput = maxCurrentInput;
        this.maxCurrentOutput = maxCurrentOutput;
        this.voltageTier = voltageTier;
        long voltage = voltageTier.value;
        this.maxInsert = Math.multiplyExact(voltage, maxCurrentInput);
        this.maxExtract = Math.multiplyExact(voltage, maxCurrentOutput);
        this.energy = energy;
        this.capacity = capacity;
    }

    @Override
    public long getEUCapacity() {
        return capacity;
    }

    @Override
    public long getEUAmount() {
        return energy;
    }

    @Override
    public long insertEU(long amount, TransactionContext transaction) {
        Util.checkNonNegative(amount);
        long inserted = Math.min(Math.min(maxInsert, amount), capacity - energy);
        if (inserted > 0) {
            journal.updateSnapshots(transaction);
            energy += inserted;
            return inserted;
        }
        return 0;
    }

    @Override
    public long extractEU(long amount, TransactionContext transaction) {
        Util.checkNonNegative(amount);
        long extracted = Math.min(Math.min(maxExtract, amount), energy);
        if (extracted > 0) {
            journal.updateSnapshots(transaction);
            energy -= extracted;
            return extracted;
        }
        return 0;
    }

    @Override
    public EUTransferInfo insertWith(VoltageTier voltageTier, long power, TransactionContext transaction) {
        Util.checkNonNegative(power);
        var inputVoltage = VoltageTier.min(this.voltageTier, voltageTier);
        var max = inputVoltage.calculatePower(maxCurrentOutput);
        power = Math.min(power, max);
        return EUTransferInfo.power(inputVoltage, insertEU(power, transaction));
    }

    @Override
    public EUTransferInfo extractWith(VoltageTier voltageTier, long power, TransactionContext transaction) {
        Util.checkNonNegative(power);
        var outputVoltage = VoltageTier.min(this.voltageTier, voltageTier);
        var max = outputVoltage.calculatePower(maxCurrentOutput);
        power = Math.min(power, max);
        return EUTransferInfo.power(outputVoltage, extractEU(power, transaction));
    }

    public void set(long eu) {
        energy = Math.clamp(eu, 0, capacity);
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        if (disallowEnergyConversion()) {
            return 0;
        }
        return EUEnergyHandler.super.insert(amount, transaction);
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putLong("energy", energy);
    }

    @Override
    public void deserialize(ValueInput input) {
        energy = Math.max(input.getLongOr("energy", 0), 0);
    }

    @Override
    public VoltageTier getVoltageTier() {
        return voltageTier;
    }

    private class EUJournal extends SnapshotJournal<Long> {
        @Override
        protected Long createSnapshot() {
            return energy;
        }

        @Override
        protected void revertToSnapshot(Long snapshot) {
            if (energy != snapshot) {
                energy = snapshot;
            }
        }
    }
}
