package com.mistbeyond.examplemod.core.logistic.energy;

import com.mistbeyond.examplemod.core.energy.VoltageTierLimited;

/**
 * Do not implement both {@link IEnergyGenerator} and {@link IEnergyConsumer} at the same time.
 * For transformers, implement {@link IEnergyTransformer} instead.
 */
public non-sealed interface IEnergyConsumer extends IEnergyComponent, VoltageTierLimited {
    EUTransferInfo insertEU();
}
