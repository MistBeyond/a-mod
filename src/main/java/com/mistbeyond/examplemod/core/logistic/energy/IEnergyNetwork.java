package com.mistbeyond.examplemod.core.logistic.energy;

import net.minecraft.server.level.ServerLevel;

import java.util.Set;

public interface IEnergyNetwork {
    Set<IEnergyGenerator> getGenerators();

    Set<IEnergyConsumer> getConsumers();

    ServerLevel getNetworkLevel();

    default boolean isClientSide() {
        return false;
    }

    void requestEnergy(IEnergyConsumer energyConsumer, EUTransferInfo info);

    void cancelRequestEnergy(IEnergyConsumer energyConsumer, EUTransferInfo info);

    void onComponentStateChanged(IEnergyNetworkComponent component);
}
