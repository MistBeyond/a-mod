package com.mistbeyond.examplemod.core.logistic.energy;

import com.mistbeyond.examplemod.core.logistic.IConnectable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import java.util.EnumSet;
import java.util.Set;

public sealed interface IEnergyComponent extends IConnectable permits IEnergyConsumer, IEnergyGenerator, IWire {
    ServerLevel getComponentLevel();

    BlockPos getPos();

    default Set<IEnergyNetwork> getNetwork() {
        return IEnergyNetworkManager.getInstance().getNetworksAt(getComponentLevel(), getPos());
    }

    @Override
    EnumSet<Direction> connections();

    default boolean isConnectWith(IEnergyComponent other) {
        return isConnectWith(other, getPos(), other.getPos());
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
}
