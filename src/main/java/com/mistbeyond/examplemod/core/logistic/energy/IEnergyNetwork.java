package com.mistbeyond.examplemod.core.logistic.energy;

import net.minecraft.core.BlockPos;
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

    void onComponentConnectionChanged(IEnergyComponent component);

    /**
     * @return {@code true} if this network did not already contain the specified
     */
    boolean addComponent(IEnergyComponent component);

    void removeComponent(IEnergyComponent component);

    boolean isNetworkAvailableAt(BlockPos pos);
}
