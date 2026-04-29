package com.mistbeyond.examplemod.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ServerConfig {
    public static final ModConfigSpec CONFIG_SPEC;
    public static final ServerConfig instance;

    public final ModConfigSpec.BooleanValue allowEnergyConversion;

    private ServerConfig(ModConfigSpec.Builder builder) {
        allowEnergyConversion = builder
                .comment("Allows conversion between FE and custom unit(EU).")
                .define("allowEnergyConversion", false);
    }

    static {
        var pair = new ModConfigSpec.Builder().configure(ServerConfig::new);
        instance = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }
}
