package com.mistbeyond.examplemod.core.logistic.energy;

import net.minecraft.core.Direction;
import org.jetbrains.annotations.ApiStatus;

import java.util.Set;

public non-sealed interface IWire extends IEnergyNetworkComponent {
    EUTransferInfo getElectricLoad();

    /**
     * Unit: EU/Current^2
     */
    long getResistance();

    Set<Direction> connections();

    @ApiStatus.NonExtendable
    default boolean isConnectTo(Direction direction) {
        return connections().contains(direction);
    }

    @Override
    default boolean allowInputFrom(Direction direction) {
        return isConnectTo(direction);
    }

    @Override
    default boolean allowOutputTo(Direction direction) {
        return isConnectTo(direction);
    }
}
