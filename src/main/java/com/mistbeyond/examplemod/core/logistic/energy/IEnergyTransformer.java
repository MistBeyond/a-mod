package com.mistbeyond.examplemod.core.logistic.energy;

import com.mistbeyond.examplemod.core.VoltageTier;
import com.mistbeyond.examplemod.core.energy.VoltageTierLimited;
import org.jetbrains.annotations.ApiStatus;

public interface IEnergyTransformer extends IEnergyGenerator, IEnergyConsumer, VoltageTierLimited {
    VoltageTier getInputVoltageTier();

    VoltageTier getOutputVoltageTier();

    /**
     * Gets the input voltage.
     */
    @Override
    default VoltageTier getVoltageTier() {
        return getInputVoltageTier();
    }

    @Override
    @ApiStatus.NonExtendable
    default boolean isTransformer() {
        return true;
    }


}
