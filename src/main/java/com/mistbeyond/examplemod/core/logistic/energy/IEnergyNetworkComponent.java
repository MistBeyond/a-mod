package com.mistbeyond.examplemod.core.logistic.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import java.util.Set;

public sealed interface IEnergyNetworkComponent permits IWire, IEnergyConsumer, IEnergyGenerator {
    ServerLevel getComponentLevel();

    BlockPos getComponentPos();

    default Set<IEnergyNetwork> getNetwork() {
        return IEnergyNetworkManager.getInstance().getNetworkAt(getComponentLevel(), getComponentPos());
    }

    boolean allowInputFrom(Direction direction);

    boolean allowOutputTo(Direction direction);
}
