package com.mistbeyond.examplemod.core.logistic.energy;

/**
 * Do not implement both {@link IWire} and the others at the same time.
 */
public non-sealed interface IWire extends IEnergyComponent {
    EUTransferInfo getElectricLoad();

    void applyElectricLoad(EUTransferInfo electricLoad);

    /**
     * Unit: EU/Current^2
     */
    long getResistance();

    /**
     * Destroys this wire because its current load has remained over the wire's rating.
     * Implementations are responsible for removing the block and applying destruction effects.
     */
    void meltdown();
}
