package com.mistbeyond.examplemod.core.logistic.energy;

import com.mistbeyond.examplemod.core.VoltageTier;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/**
 * Do not implement both {@link IEnergyGenerator} and {@link IEnergyConsumer} at the same time.
 * For transformers, implement {@link IEnergyTransformer} instead.
 */
public non-sealed interface IEnergyGenerator extends IEnergyComponent {
    /**
     * Extracts energy from this generator.
     *
     * @param amount      the amount to extract, in EU
     * @param transaction the transaction to record the extraction in, or {@code null} when there
     *                    is no enclosing transaction; implementations may then apply the change
     *                    immediately without rollback support
     * @return the actually extracted transfer info; power may be less than {@code amount} when the
     * generator is empty or its current limits are exceeded
     */
    EUTransferInfo extractEnergy(long amount, @Nullable TransactionContext transaction);

    VoltageTier getGeneratorVoltageTier();

    @ApiStatus.NonExtendable
    default boolean isTransformer() {
        return false;
    }
}
