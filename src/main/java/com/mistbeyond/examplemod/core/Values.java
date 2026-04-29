package com.mistbeyond.examplemod.core;

public final class Values {
    // unit: Example Mod - EnergyUnit(= FE*4 = Voltage * Ampere * 1t)/Voltage(=long)/Ampere(=long) not FE
    public static final long TO_FE = 4;
    public static final long DEFAULT_IO_CURRENT = 2;
    public static final long DEFAULT_OUTPUT_CURRENT = 16;
    public static final long GAME_HOUR_TICKS = 20 * 60;
    public static final long KILOWATT_HOUR = 1000 * GAME_HOUR_TICKS;
    public static final long DEFAULT_WORKING_CURRENT = 1;

    private Values() {
    }

    /**
     * Calculates the energy capacity at the given voltage and current over 20 ticks.
     *
     * @param ioVoltage the voltage level
     * @param ioCurrent the current in amperes
     * @return the energy capacity (EU) for the specified voltage and current
     */
    public static long calculateCapacity(long ioVoltage, long ioCurrent) {
        return ioVoltage * ioCurrent * 200;
    }

    /**
     * Calculates the energy capacity at the given voltage with the default input
     * current ({@link #DEFAULT_IO_CURRENT} amperes) over 20 ticks.
     *
     * @param voltage the voltage level
     * @return the energy capacity (EU) for the specified voltage at default current
     */
    public static long capacityForVoltage(long voltage) {
        return calculateCapacity(voltage, DEFAULT_IO_CURRENT);
    }
}
