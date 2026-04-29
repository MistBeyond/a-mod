package com.mistbeyond.examplemod.core.energy;

import com.mistbeyond.examplemod.core.VoltageTier;

public interface VoltageTierLimited {
    VoltageTier getVoltageTier();

    /**
     * Checks whether the given voltage is within the acceptable range (typically other ≤ this).
     */
    default boolean isVoltageTierSafe(VoltageTier other) {
        return getVoltageTier().isSafe(other);
    }

    default void onVoltageTierUnsafe() {
    }

    default void checkVoltageTier(VoltageTier other) {
        if (!isVoltageTierSafe(other)) {
            this.onVoltageTierUnsafe();
        }
    }
}
