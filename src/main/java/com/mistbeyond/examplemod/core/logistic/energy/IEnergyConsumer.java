package com.mistbeyond.examplemod.core.logistic.energy;

import com.mistbeyond.examplemod.core.energy.VoltageTierLimited;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

/**
 * Do not implement both {@link IEnergyGenerator} and {@link IEnergyConsumer} at the same time.
 * For transformers, implement {@link IEnergyTransformer} instead.
 */
public non-sealed interface IEnergyConsumer extends IEnergyComponent, VoltageTierLimited {
    /**
     * Accepts energy into this consumer.
     *
     * @param info        the transfer info offered by the network (voltage tier and power)
     * @param transaction the transaction to record the insertion in, or {@code null} when there is
     *                    no enclosing transaction; implementations may then apply the change
     *                    immediately without rollback support
     * @return the actually accepted transfer info; power may be less than offered, e.g. when the
     * consumer's buffer is nearly full or the voltage/current limits are exceeded
     */
    EUTransferInfo insertEU(EUTransferInfo info, @Nullable TransactionContext transaction);
}
