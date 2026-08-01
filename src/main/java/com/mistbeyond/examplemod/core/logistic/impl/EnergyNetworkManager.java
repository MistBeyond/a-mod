package com.mistbeyond.examplemod.core.logistic.impl;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mistbeyond.examplemod.core.logistic.energy.IEnergyNetwork;
import com.mistbeyond.examplemod.core.logistic.energy.IEnergyNetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EnergyNetworkManager implements IEnergyNetworkManager {
    public static final IEnergyNetworkManager INSTANCE = new EnergyNetworkManager();
    private final Multimap<ResourceKey<Level>, IEnergyNetwork> networks = HashMultimap.create();

    private static void notImplemented() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public Set<IEnergyNetwork> getNetworksAt(ServerLevel level, BlockPos pos) {
        return networks.get(level.dimension()).stream()
                .filter(n -> n.isNetworkAvailableAt(pos))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Collection<IEnergyNetwork> getNetworks(ServerLevel level) {
        return Set.copyOf(networks.get(level.dimension()));
    }

    @Override
    public IEnergyNetwork mergeNetwork(ServerLevel level, BlockPos where) {
        notImplemented();
        var networks = List.copyOf(getNetworksAt(level, where));
        if (networks.isEmpty()) {
            throw new IllegalArgumentException(("No networks found at %s %s").formatted(level.dimension(), where));
        }
        return null;
    }
}
