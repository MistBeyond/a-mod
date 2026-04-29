package com.mistbeyond.examplemod.util;

import com.google.common.primitives.Ints;
import com.mistbeyond.examplemod.core.Values;

public final class EnergyUtil {
    private EnergyUtil() {
    }

    public static long saturatingToFE(long eu) {
        Util.checkNonNegative(eu);
        return Util.saturatedPositiveMultiply(eu, Values.TO_FE);
    }

    public static int saturatingToIntFE(long eu) {
        return Ints.saturatedCast(saturatingToFE(eu));
    }

    public static long toEU(long fe) {
        Util.checkNonNegative(fe);
        return fe / Values.TO_FE;
    }
}
