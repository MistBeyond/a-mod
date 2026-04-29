package com.mistbeyond.examplemod.core.registry.impl;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

public interface Checker {
    @CanIgnoreReturnValue
    boolean inheritanceChainCheck(Class<?> clazz, CheckReport.Adder adder);

    /**
     * Run check.
     */
    void check();
}
