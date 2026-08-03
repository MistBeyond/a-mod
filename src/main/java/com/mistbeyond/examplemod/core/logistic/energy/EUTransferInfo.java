package com.mistbeyond.examplemod.core.logistic.energy;

import com.mistbeyond.examplemod.core.VoltageTier;
import com.mistbeyond.examplemod.util.Util;

/**
 * An immutable description of an EU transfer.
 *
 * <p>{@link #voltageTier()} is the nominal tier, fixed at construction time and used for safety
 * checks and ranking. The actual {@link #voltage()} is the computational value and may decay
 * (e.g. line loss) but never exceeds the nominal tier value.
 *
 * <p>{@link #power()} is the average energy per tick ({@link #totalEnergy()} / {@link #duration()},
 * integer division). Runtime transfer and machine draws use tick-level infos ({@code duration == 1});
 * {@link #of(long, float, int)} describes batch totals and supports fractional current.
 */
public record EUTransferInfo(VoltageTier voltageTier, long voltage, long totalEnergy, int duration) {
    /**
     * It's a immutable info.
     */
    public static final EUTransferInfo ZERO = new EUTransferInfo(VoltageTier.ZERO, 0, 0, 1);

    public EUTransferInfo {
        Util.checkNonNegative(voltage);
        Util.checkNonNegative(totalEnergy);
        Util.checkNonNegative(duration);
        if (voltage > voltageTier.value) {
            throw new IllegalArgumentException("voltage " + voltage + " exceeds its voltage tier " + voltageTier.value);
        }
    }

    /**
     * Use {@link EUTransferInfo#power(long, long)} instead to creating a power info.
     */
    public static EUTransferInfo of(long voltage, float current, int duration) {
        Util.checkNonNegative(voltage);
        Util.checkNonNegative(duration);
        Util.checkNonNegative(current);
        return new EUTransferInfo(VoltageTier.of(voltage), voltage, Math.round((double) current * voltage * duration), duration);
    }

    public static EUTransferInfo power(long voltage, long power) {
        Util.checkNonNegative(voltage);
        Util.checkNonNegative(power);
        return new EUTransferInfo(VoltageTier.of(voltage), voltage, power, 1);
    }

    public static EUTransferInfo power(VoltageTier voltageTier, long power) {
        Util.checkNonNegative(power);
        return new EUTransferInfo(voltageTier, voltageTier.value, power, 1);
    }

    public float current() {
        if (duration == 0 || voltage == 0) {
            return 0;
        }
        return (float) totalEnergy / duration / voltage;
    }

    public long power() {
        return switch (duration) {
            case 0 -> 0;
            case 1 -> totalEnergy;
            default -> totalEnergy / duration;
        };
    }
}
