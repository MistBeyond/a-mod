package com.mistbeyond.examplemod.core.energy;

import com.mistbeyond.examplemod.util.Util;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class LongBasicHandler implements EnergyHandler, ValueIOSerializable {
    private final EnergyJournal energyJournal = new EnergyJournal();
    private final long capacity;
    private long energy;

    public LongBasicHandler(long capacity) {
        this(capacity, 0);
    }

    public LongBasicHandler(long capacity, long energy) {
        Util.checkNonNegative(capacity);
        Util.checkNonNegative(energy);
        this.capacity = capacity;
        this.energy = Math.min(energy, capacity);
    }

    @Override
    public long getAmountAsLong() {
        return energy;
    }

    @Override
    public long getCapacityAsLong() {
        return capacity;
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        return Math.toIntExact(insert((long) amount, transaction));
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        return Math.toIntExact(extract((long) amount, transaction));
    }

    public long insert(long amount, TransactionContext transaction) {
        Util.checkNonNegative(amount);
        long inserted = Math.min(amount, capacity - amount);
        if (inserted > 0) {
            energyJournal.updateSnapshots(transaction);
            energy += inserted;
            return inserted;
        }
        return 0;
    }

    public long extract(long amount, TransactionContext transaction) {
        Util.checkNonNegative(amount);
        long extracted = Math.min(amount, energy);
        if (extracted > 0) {
            energyJournal.updateSnapshots(transaction);
            energy -= extracted;
            return extracted;
        }
        return 0;
    }

    public void set(long amount) {
        Util.checkNonNegative(amount);
        energy = amount;
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putLong("energy", energy);
    }

    @Override
    public void deserialize(ValueInput input) {
        energy = Math.max(input.getLongOr("energy", 0), 0);
    }

    private class EnergyJournal extends SnapshotJournal<Long> {
        @Override
        protected Long createSnapshot() {
            return energy;
        }

        @Override
        protected void revertToSnapshot(Long snapshot) {
            energy = snapshot;
        }
    }
}
