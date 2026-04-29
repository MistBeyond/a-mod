package com.mistbeyond.examplemod.core.logistic.energy;

import com.mistbeyond.examplemod.core.VoltageTier;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public non-sealed interface IEnergyGenerator extends IEnergyNetworkComponent {
    EUTransferInfo extractEnergy(long amount, TransactionContext transaction);

    VoltageTier getGeneratorVoltageTier();

    /**
     * energy > 0
     */
    boolean hasEnergy();
}
