package com.mistbeyond.examplemod.block.entity.machine;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.mistbeyond.examplemod.core.energy.EUEnergyHandler;
import com.mistbeyond.examplemod.core.logistic.energy.EUTransferInfo;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public interface ElectricMachine {
    EUEnergyHandler getEnergyHandler();

    @CanIgnoreReturnValue
    EUTransferInfo charge(ServerLevel serverLevel, TransactionContext transaction);
}
