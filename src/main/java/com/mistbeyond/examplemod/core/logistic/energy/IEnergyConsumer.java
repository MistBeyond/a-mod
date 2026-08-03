package com.mistbeyond.examplemod.core.logistic.energy;

import com.mistbeyond.examplemod.core.energy.VoltageTierLimited;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Do not implement both {@link IEnergyGenerator} and {@link IEnergyConsumer} at the same time.
 * For transformers, implement {@link IEnergyTransformer} instead.
 */
public non-sealed interface IEnergyConsumer extends IEnergyComponent, VoltageTierLimited {
    /**
     * Accepts energy into this consumer.
     *
     * @param info        the transfer info offered by the network (voltage tier and power)
     * @param transaction the transaction to record the insertion in
     * @return the actually accepted transfer info; power may be less than offered, e.g. when the
     * consumer's buffer is nearly full or the voltage/current limits are exceeded
     */
    EUTransferInfo insertEU(EUTransferInfo info, TransactionContext transaction);
}
