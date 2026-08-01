package com.mistbeyond.examplemod.core.logistic.energy;

import com.mistbeyond.examplemod.core.VoltageTier;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.ApiStatus;

/**
 * Do not implement both {@link IEnergyGenerator} and {@link IEnergyConsumer} at the same time.
 * For transformers, implement {@link IEnergyTransformer} instead.
 */
public non-sealed interface IEnergyGenerator extends IEnergyComponent {
    EUTransferInfo extractEnergy(long amount, TransactionContext transaction);

    VoltageTier getGeneratorVoltageTier();

    @ApiStatus.NonExtendable
    default boolean isTransformer() {
        return false;
    }
}
