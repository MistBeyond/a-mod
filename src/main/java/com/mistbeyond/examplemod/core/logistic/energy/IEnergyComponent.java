package com.mistbeyond.examplemod.core.logistic.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import java.util.EnumSet;
import java.util.Set;

public sealed interface IEnergyComponent permits IEnergyConsumer, IEnergyGenerator, IWire {
    ServerLevel getComponentLevel();

    BlockPos getPos();

    default Set<IEnergyNetwork> getNetwork() {
        return IEnergyNetworkManager.getInstance().getNetworksAt(getComponentLevel(), getPos());
    }

    EnumSet<Direction> connections();

    /**
     * {@code IEnergyComponent this d| -> Any }
     */
    default boolean isConnectTo(Direction direction) {
        return connections().contains(direction);
    }

    /**
     * {@code a.isConnectWith(b) <=> b.isConnectWith(a)}
     * <p>
     * {@code (IEnergyComponent this) d| <-> |d.opposite (IEnergyComponent other)}
     */
    default boolean isConnectWith(IEnergyComponent other) {
        if (!isNeighbour(other)) {
            return false;
        }
        for (Direction direction : connections()) {
            if (other.isConnectTo(direction.getOpposite())) {
                return true;
            }
        }
        return false;
    }

    default void onConnectionChanged() {
        getNetwork().forEach(n -> n.onComponentConnectionChanged(this));
    }


    default void addToNetworks() {
        getNetwork().forEach(n -> n.addComponent(this));
    }

    default void removeFromNetworks() {
        getNetwork().forEach(n -> n.removeComponent(this));
    }

    private boolean isNeighbour(IEnergyComponent other) {
        return this.getPos().closerThan(other.getPos(), 1.01);
    }
}
