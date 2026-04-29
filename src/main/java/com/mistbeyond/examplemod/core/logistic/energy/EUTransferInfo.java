package com.mistbeyond.examplemod.core.logistic.energy;

import com.mistbeyond.examplemod.core.VoltageTier;
import com.mistbeyond.examplemod.util.Util;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class EUTransferInfo {
    /**
     * It's a immutable info.
     */
    public static final EUTransferInfo ZERO = new EUTransferInfo(VoltageTier.ZERO, 0, 0, 1);
    private final VoltageTier voltageTier;
    private final long voltage;
    private final long totalEnergy;
    private final int duration;

    private EUTransferInfo(VoltageTier voltageTier, long voltage, long totalEnergy, int duration) {
        this.voltageTier = voltageTier;
        this.voltage = voltage;
        this.totalEnergy = totalEnergy;
        this.duration = duration;
    }

    /**
     * Use {@link EUTransferInfo#power(long, long)} instead to creating a power info.
     */
    public static EUTransferInfo of(long voltage, float current, int duration) {
        Util.checkNonNegative(voltage);
        Util.checkNonNegative(duration);
        Util.checkNonNegative(current);
        return new EUTransferInfo(VoltageTier.of(voltage), voltage, (long) (current * voltage * duration), duration);
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

    public long voltage() {
        return voltage;
    }

    public float current() {
        if (duration == 0 || voltage == 0) {
            return 0;
        }
        return (float) totalEnergy / duration / voltage;
    }

    public int duration() {
        return duration;
    }

    public VoltageTier voltageTier() {
        return voltageTier;
    }

    public long totalEnergy() {
        return totalEnergy;
    }

    public long power() {
        return switch (duration) {
            case 0 -> 0;
            case 1 -> totalEnergy;
            default -> totalEnergy / duration;
        };
    }
}
