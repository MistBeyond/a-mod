package com.mistbeyond.examplemod.core.energy;

import com.mistbeyond.examplemod.config.ServerConfig;

import java.util.function.BooleanSupplier;

public interface EnergyConversionPermission {
    BooleanSupplier thisModChecker = ServerConfig.instance.allowEnergyConversion::isTrue;

    default boolean allowEnergyConversion() {
        return thisModChecker.getAsBoolean();
    }

    default boolean disallowEnergyConversion() {
        return !allowEnergyConversion();
    }
}
