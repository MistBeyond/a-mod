package com.mistbeyond.examplemod.core.logistic.impl;

import com.mistbeyond.examplemod.core.logistic.energy.IEnergyNetwork;
import com.mistbeyond.examplemod.core.logistic.energy.IEnergyNetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class EnergyNetworkManager implements IEnergyNetworkManager {
    public static final IEnergyNetworkManager INSTANCE = new EnergyNetworkManager();

    @Override
    public Set<IEnergyNetwork> getNetworkAt(ServerLevel level, BlockPos pos) {
        return null;
    }

    @Override
    public Collection<IEnergyNetwork> getNetworks() {
        return List.of();
    }
}
