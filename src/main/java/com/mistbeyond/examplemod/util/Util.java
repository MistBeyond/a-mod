package com.mistbeyond.examplemod.util;

import org.jetbrains.annotations.Range;

public final class Util {
    private Util() {
    }

    public static long saturatedPositiveMultiply(@Range(from = 0, to = Long.MAX_VALUE) long a, @Range(from = 0, to = Long.MAX_VALUE) long b) {
        checkNonNegative(a);
        checkNonNegative(b);
        if (a == 0 || b == 0) return 0;
        return Long.MAX_VALUE / a >= b ? a * b : Long.MAX_VALUE;
    }

    public static void checkNonNegative(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Expected value to be non-negative: " + value);
        }
    }

    public static void checkNonNegative(double value) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("Expected value to be finite: " + value);
        }
        if (value < 0) {
            throw new IllegalArgumentException("Expected value to be non-negative: " + value);
        }
    }
}
