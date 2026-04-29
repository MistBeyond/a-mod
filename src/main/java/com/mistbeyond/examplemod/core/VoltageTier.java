package com.mistbeyond.examplemod.core;

import com.mistbeyond.examplemod.util.Util;
import net.minecraft.network.chat.Component;

public enum VoltageTier {
    ZERO(0, "§7"), LOW(32, "§a"), MEDIUM(256, "§e"),
    HIGH(2048, "§6"), ULTRA_HIGH(32768, "§c"), MAX(Long.MAX_VALUE, "§4");
    private static final VoltageTier[] VALUES = values();
    public final long value;
    public final String color;
    private final String string;
    private final Component component;

    VoltageTier(long value, String color) {
        this.value = value;
        this.color = color;
        string = color + this.name().replace("_", " ") + "§r";
        component = Component.literal(string);
    }

    public static VoltageTier of(long value) {
        Util.checkNonNegative(value);
        for (VoltageTier tier : VALUES) {
            if (tier.isSafe(value)) {
                return tier;
            }
        }
        return MAX;
    }

    public static VoltageTier min(VoltageTier a, VoltageTier b) {
        return a.value < b.value ? a : b;
    }

    public static VoltageTier max(VoltageTier a, VoltageTier b) {
        return a.value > b.value ? a : b;
    }

    public boolean isSafe(long other) {
        return this.value >= other;
    }

    public boolean isSafe(VoltageTier other) {
        return this.isSafe(other.value);
    }

    public long calculatePower(long current) {
        Util.checkNonNegative(current);
        return Math.multiplyExact(this.value, current);
    }

    public Component asComponent() {
        return component;
    }

    public String asString() {
        return string;
    }

    public long value() {
        return value;
    }
}
