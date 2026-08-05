package com.mistbeyond.examplemod.core.logistic.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

import java.util.Set;

public interface IEnergyNetwork {
    Set<IEnergyGenerator> getGenerators();

    Set<IEnergyConsumer> getConsumers();

    ServerLevel getNetworkLevel();

    default boolean isClientSide() {
        return false;
    }

    /**
     * Pulls energy from this network for the given consumer, immediately.
     * <p>
     * The network iterates the consumer's routes in priority order (non-transformer generators
     * first, then higher voltage, then lower total resistance, then shorter paths). A single
     * generator must cover the whole request, including line loss on its route; if it cannot,
     * the next route is tried instead.
     * <p>
     * Line loss is {@code totalResistance * current^2} per tick and is drawn from the generator
     * side, so the generator supplies {@code requested + loss} while the consumer receives
     * {@code requested}. Wires on the used route are notified via
     * {@link IWire#applyElectricLoad(EUTransferInfo)}.
     *
     * @param consumer    the consumer requesting energy
     * @param info        the requested transfer info; only {@link EUTransferInfo#power()} is used,
     *                    i.e. this is a per-tick pull
     * @param transaction the transaction to record the transfer in, or {@code null} to open a
     *                    root transaction
     * @return the actually delivered transfer info, or {@link EUTransferInfo#ZERO} when no
     * generator route could satisfy the request
     */
    EUTransferInfo pullEnergy(IEnergyConsumer consumer, EUTransferInfo info, @Nullable TransactionContext transaction);

    void onComponentConnectionChanged(IEnergyComponent component);

    /**
     * @return {@code true} if this network did not already contain the specified
     */
    boolean addComponent(IEnergyComponent component);

    void removeComponent(IEnergyComponent component);

    boolean isNetworkAvailableAt(BlockPos pos);
}
