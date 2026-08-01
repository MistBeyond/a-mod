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
}
