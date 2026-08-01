package com.mistbeyond.examplemod.core.logistic.energy;

import com.mistbeyond.examplemod.core.logistic.impl.EnergyNetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.Collection;
import java.util.Set;

public interface IEnergyNetworkManager {
    static IEnergyNetworkManager getInstance() {
        return EnergyNetworkManager.INSTANCE;
    }

    Set<IEnergyNetwork> getNetworksAt(ServerLevel level, BlockPos pos);

    Collection<IEnergyNetwork> getNetworks(ServerLevel level);

    /**
     * @return merged network
     */
    IEnergyNetwork mergeNetwork(ServerLevel level, BlockPos where);
}
