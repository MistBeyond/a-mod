package com.mistbeyond.examplemod.core.energy;

public interface CurrentLimited {
    long getMaxOutputCurrent();

    long getMaxInputCurrent();

    default boolean canHandleInput(long other) {
        return getMaxInputCurrent() >= other;
    }

    default boolean canHandleOutput(long other) {
        return getMaxOutputCurrent() >= other;
    }

    default boolean canHandleBoth(long otherInput, long otherOutput) {
        return canHandleInput(otherInput) && canHandleOutput(otherOutput);
    }
}
