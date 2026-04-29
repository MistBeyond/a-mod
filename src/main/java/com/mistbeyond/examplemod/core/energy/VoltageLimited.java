package com.mistbeyond.examplemod.core.energy;

public interface VoltageLimited {
    /**
     * Checks whether the given voltage is within the acceptable range (typically other ≤ this).
     */
    default boolean isVoltageSafe(long otherVoltage) {
        return getVoltage() >= otherVoltage;
    }

    long getVoltage();
}
