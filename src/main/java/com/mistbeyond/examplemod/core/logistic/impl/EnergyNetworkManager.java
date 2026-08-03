package com.mistbeyond.examplemod.core.logistic.impl;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mistbeyond.examplemod.core.logistic.energy.IEnergyComponent;
import com.mistbeyond.examplemod.core.logistic.energy.IEnergyNetwork;
import com.mistbeyond.examplemod.core.logistic.energy.IEnergyNetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class EnergyNetworkManager implements IEnergyNetworkManager {
    public static final EnergyNetworkManager INSTANCE = new EnergyNetworkManager();
    private final Multimap<ResourceKey<Level>, IEnergyNetwork> networks = HashMultimap.create();
    private final Map<ResourceKey<Level>, Set<IEnergyComponent>> pendingLoads = new HashMap<>();

    private EnergyNetworkManager() {
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
        if (!(level.getBlockEntity(where) instanceof IEnergyComponent component)) {
            throw new IllegalArgumentException(("No energy component found at %s %s").formatted(level.dimension(), where));
        }
        Set<IEnergyNetwork> existing = getNetworksAt(level, where);
        if (!existing.isEmpty()) {
            return existing.iterator().next();
        }
        EnergyNetwork network = new EnergyNetwork(level);
        Set<IEnergyNetwork> merged = network.startMergeFrom(component);
        for (IEnergyNetwork mergedNetwork : merged) {
            networks.remove(level.dimension(), mergedNetwork);
        }
        register(network);
        return network;
    }

    @Override
    public void onComponentLoaded(IEnergyComponent component) {
        ResourceKey<Level> dimension = component.getComponentLevel().dimension();
        pendingLoads.computeIfAbsent(dimension, _ -> new HashSet<>()).add(component);
    }

    @Override
    public void onComponentRemoved(IEnergyComponent component) {
        ServerLevel level = component.getComponentLevel();
        Set<IEnergyComponent> pending = pendingLoads.get(level.dimension());
        if (pending != null) {
            pending.remove(component);
        }
        for (IEnergyNetwork network : Set.copyOf(getNetworksAt(level, component.getPos()))) {
            network.removeComponent(component);
        }
    }

    /**
     * Processes the pending merges of the given level. Called once per server tick.
     */
    void processPendingLoads(ServerLevel level) {
        Set<IEnergyComponent> pending = pendingLoads.remove(level.dimension());
        if (pending == null || pending.isEmpty()) {
            return;
        }
        for (IEnergyComponent component : pending) {
            Object loaded = level.getBlockEntity(component.getPos());
            if (loaded == component) {
                mergeNetwork(level, component.getPos());
            }
        }
    }

    /**
     * Clears all state of the given level, e.g. when the level unloads.
     */
    void onLevelUnload(ServerLevel level) {
        networks.removeAll(level.dimension());
        pendingLoads.remove(level.dimension());
    }

    /**
     * Clears all state, e.g. when the server stops.
     */
    void clearAll() {
        networks.clear();
        pendingLoads.clear();
    }

    void register(IEnergyNetwork network) {
        networks.put(network.getNetworkLevel().dimension(), network);
    }

    void unregister(IEnergyNetwork network) {
        networks.remove(network.getNetworkLevel().dimension(), network);
    }
}
