package com.mistbeyond.examplemod.core.logistic.energy;

import com.mistbeyond.examplemod.core.VoltageTier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EUTransferInfoTest {

    @Test
    void zeroHasNoPowerOrCurrent() {
        assertEquals(0, EUTransferInfo.ZERO.power());
        assertEquals(0L, EUTransferInfo.ZERO.totalEnergy());
        assertEquals(0f, EUTransferInfo.ZERO.current());
    }

    @Test
    void zeroDurationHasNoPower() {
        EUTransferInfo info = new EUTransferInfo(VoltageTier.LOW, 32, 100, 0);

        assertEquals(0, info.power());
        assertEquals(0f, info.current());
    }

    @Test
    void fractionalCurrentComputesTotalEnergyAndPower() {
        EUTransferInfo info = EUTransferInfo.of(32, 0.5f, 20);

        assertEquals(320, info.totalEnergy());
        assertEquals(16, info.power());
        assertEquals(0.5f, info.current());
    }

    @Test
    void powerWithTierUsesTierVoltage() {
        EUTransferInfo info = EUTransferInfo.power(VoltageTier.LOW, 64);

        assertEquals(32, info.voltage());
        assertEquals(64, info.totalEnergy());
        assertEquals(1, info.duration());
        assertEquals(2f, info.current());
    }

    @Test
    void voltageAboveTierThrows() {
        assertThrows(IllegalArgumentException.class, () -> new EUTransferInfo(VoltageTier.LOW, 33, 1, 1));
    }

    @Test
    void negativeArgumentsThrow() {
        assertThrows(IllegalArgumentException.class, () -> EUTransferInfo.power(VoltageTier.LOW, -1));
        assertThrows(IllegalArgumentException.class, () -> EUTransferInfo.power(-1, 1));
        assertThrows(IllegalArgumentException.class, () -> EUTransferInfo.of(32, -1, 1));
    }
}
