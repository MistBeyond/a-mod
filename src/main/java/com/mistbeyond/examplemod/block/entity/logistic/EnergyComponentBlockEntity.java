package com.mistbeyond.examplemod.block.entity.logistic;

import com.mistbeyond.examplemod.core.logistic.energy.IEnergyComponent;
import com.mistbeyond.examplemod.core.logistic.energy.IEnergyNetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Abstract base class for block entities that participate in the EU energy network.
 * <p>
 * Subclasses should implement one of the sealed energy component roles
 * ({@link com.mistbeyond.examplemod.core.logistic.energy.IEnergyConsumer},
 * {@link com.mistbeyond.examplemod.core.logistic.energy.IEnergyGenerator} or
 * {@link com.mistbeyond.examplemod.core.logistic.energy.IWire}); the convenience methods
 * {@link #getPos()} and {@link #getComponentLevel()} already satisfy the corresponding
 * {@link com.mistbeyond.examplemod.core.logistic.energy.IEnergyComponent} methods.
 * <p>
 * The network join/leave lifecycle is handled here:
 * <ul>
 *     <li>{@link #onLoad()} registers the component for a deferred merge into the network.
 *     The merge itself runs on the next server tick, because level interactions during chunk
 *     loading may deadlock the game.</li>
 *     <li>{@link #setRemoved()} removes the component from all networks it belongs to,
 *     splitting networks if necessary. This covers block breaking, block replacement,
 *     {@code /clone} and structure placement, and chunk unloading.</li>
 * </ul>
 * Both hooks only act on the server side.
 */
public abstract class EnergyComponentBlockEntity extends BlockEntity {

    public EnergyComponentBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) {
        super(type, worldPosition, blockState);
    }

    /**
     * Convenience implementation of {@link IEnergyComponent#getComponentLevel()}.
     */
    public ServerLevel getComponentLevel() {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel;
        }
        throw new IllegalStateException("Energy component %s @ %s is not on the server side".formatted(getType(), worldPosition));
    }

    /**
     * Convenience implementation of {@link IEnergyComponent#getPos()}.
     */
    public BlockPos getPos() {
        return worldPosition;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel && this instanceof IEnergyComponent component) {
            IEnergyNetworkManager.getInstance().onComponentLoaded(component);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level instanceof ServerLevel && this instanceof IEnergyComponent component) {
            IEnergyNetworkManager.getInstance().onComponentRemoved(component);
        }
    }
}
