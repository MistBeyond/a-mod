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

    /**
     * Called by an energy component when it is loaded into a server level, e.g. from
     * {@code BlockEntity#onLoad()}. The merge is deferred to the next server tick, because
     * level interactions during chunk loading may deadlock the game.
     *
     * @param component the component that was loaded
     */
    void onComponentLoaded(IEnergyComponent component);

    /**
     * Called by an energy component when it is removed from a server level, e.g. from
     * {@code BlockEntity#setRemoved()}. Removes the component from all networks it belongs to,
     * splitting networks if necessary and unregistering empty ones.
     *
     * @param component the component that was removed
     */
    void onComponentRemoved(IEnergyComponent component);
}
