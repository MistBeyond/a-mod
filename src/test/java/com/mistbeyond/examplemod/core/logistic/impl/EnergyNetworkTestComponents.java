package com.mistbeyond.examplemod.core.logistic.impl;

import com.mistbeyond.examplemod.core.VoltageTier;
import com.mistbeyond.examplemod.core.logistic.energy.*;
import com.mistbeyond.examplemod.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

final class EnergyNetworkTestComponents {
    private EnergyNetworkTestComponents() {
    }

    static ServerLevel mockLevel() {
        ServerLevel level = mock(ServerLevel.class);
        when(level.dimension()).thenReturn(Level.OVERWORLD);
        return level;
    }

    static void mapBlockEntities(ServerLevel level, IEnergyComponent... components) {
        for (IEnergyComponent component : components) {
            when(level.getBlockEntity(component.getPos())).thenReturn((BlockEntity) component);
        }
    }

    @SuppressWarnings("unchecked")
    static <T extends IEnergyComponent> T wrap(T delegate) {
        List<Class<?>> interfaces = new ArrayList<>();
        if (delegate instanceof IWire) {
            interfaces.add(IWire.class);
        }
        if (delegate instanceof IEnergyGenerator) {
            interfaces.add(IEnergyGenerator.class);
        }
        if (delegate instanceof IEnergyConsumer) {
            interfaces.add(IEnergyConsumer.class);
        }
        if (interfaces.isEmpty()) {
            throw new IllegalArgumentException("Not an energy component: " + delegate);
        }
        BlockEntity blockEntity = mock(BlockEntity.class, withSettings().extraInterfaces(interfaces.toArray(Class[]::new)));
        IEnergyComponent component = (IEnergyComponent) blockEntity;
        when(component.getPos()).thenReturn(delegate.getPos());
        when(component.getComponentLevel()).thenReturn(delegate.getComponentLevel());
        when(component.connections()).thenReturn(delegate.connections());
        when(component.isConnectTo(any(Direction.class))).thenAnswer(inv -> delegate.isConnectTo(inv.getArgument(0)));
        when(component.isConnectWith(any(IEnergyComponent.class))).thenAnswer(inv -> delegate.isConnectWith(inv.getArgument(0)));
        when(component.getNetwork()).thenAnswer(inv -> delegate.getNetwork());
        doAnswer(inv -> {
            delegate.onConnectionChanged();
            return null;
        }).when(component).onConnectionChanged();
        doAnswer(inv -> {
            delegate.addToNetworks();
            return null;
        }).when(component).addToNetworks();
        doAnswer(inv -> {
            delegate.removeFromNetworks();
            return null;
        }).when(component).removeFromNetworks();
        if (delegate instanceof IWire wire) {
            IWire wrapped = (IWire) component;
            when(wrapped.getElectricLoad()).thenAnswer(inv -> wire.getElectricLoad());
            doAnswer(inv -> {
                wire.applyElectricLoad(inv.getArgument(0));
                return null;
            }).when(wrapped).applyElectricLoad(any(EUTransferInfo.class));
            when(wrapped.getResistance()).thenReturn(wire.getResistance());
        }
        if (delegate instanceof IEnergyGenerator generator) {
            IEnergyGenerator wrapped = (IEnergyGenerator) component;
            when(wrapped.extractEnergy(anyLong(), any())).thenAnswer(inv -> generator.extractEnergy(inv.getArgument(0), inv.getArgument(1)));
            when(wrapped.getGeneratorVoltageTier()).thenReturn(generator.getGeneratorVoltageTier());
            when(wrapped.isTransformer()).thenReturn(generator.isTransformer());
        }
        if (delegate instanceof IEnergyConsumer consumer) {
            IEnergyConsumer wrapped = (IEnergyConsumer) component;
            when(wrapped.insertEU(any(EUTransferInfo.class), any())).thenAnswer(inv -> consumer.insertEU(inv.getArgument(0), inv.getArgument(1)));
            when(wrapped.getVoltageTier()).thenReturn(consumer.getVoltageTier());
        }
        return (T) component;
    }

    static final class Generator implements IEnergyGenerator {
        private final VoltageTier tier;
        private final ServerLevel level;
        private final BlockPos pos;
        private final EnumSet<Direction> connections;
        private final GeneratorJournal journal = new GeneratorJournal();
        private long energy;

        Generator(VoltageTier tier, long energy, ServerLevel level, BlockPos pos, Direction... connections) {
            this.tier = tier;
            this.energy = energy;
            this.level = level;
            this.pos = pos;
            this.connections = EnumSet.noneOf(Direction.class);
            this.connections.addAll(List.of(connections));
        }

        @Override
        public EUTransferInfo extractEnergy(long amount, @Nullable TransactionContext transaction) {
            Util.checkNonNegative(amount);
            long extracted = Math.min(amount, energy);
            if (extracted > 0) {
                if (transaction != null) {
                    journal.updateSnapshots(transaction);
                }
                energy -= extracted;
            }
            return EUTransferInfo.power(tier, extracted);
        }

        @Override
        public VoltageTier getGeneratorVoltageTier() {
            return tier;
        }

        @Override
        public ServerLevel getComponentLevel() {
            return level;
        }

        @Override
        public BlockPos getPos() {
            return pos;
        }

        @Override
        public EnumSet<Direction> connections() {
            return connections;
        }

        long energy() {
            return energy;
        }

        private class GeneratorJournal extends SnapshotJournal<Long> {
            @Override
            protected Long createSnapshot() {
                return energy;
            }

            @Override
            protected void revertToSnapshot(Long snapshot) {
                energy = snapshot;
            }
        }
    }

    static final class Consumer implements IEnergyConsumer {
        private final VoltageTier tier;
        private final ServerLevel level;
        private final BlockPos pos;
        private final EnumSet<Direction> connections;
        private final long capacity;
        private final ConsumerJournal journal = new ConsumerJournal();
        private long stored;

        Consumer(VoltageTier tier, long capacity, long stored, ServerLevel level, BlockPos pos,
                 Direction... connections) {
            this.tier = tier;
            this.capacity = capacity;
            this.stored = stored;
            this.level = level;
            this.pos = pos;
            this.connections = EnumSet.noneOf(Direction.class);
            this.connections.addAll(List.of(connections));
        }

        @Override
        public EUTransferInfo insertEU(EUTransferInfo info, @Nullable TransactionContext transaction) {
            long inserted = Math.min(info.power(), capacity - stored);
            if (inserted > 0) {
                if (transaction != null) {
                    journal.updateSnapshots(transaction);
                }
                stored += inserted;
            }
            return EUTransferInfo.power(info.voltageTier(), inserted);
        }

        @Override
        public VoltageTier getVoltageTier() {
            return tier;
        }

        @Override
        public ServerLevel getComponentLevel() {
            return level;
        }

        @Override
        public BlockPos getPos() {
            return pos;
        }

        @Override
        public EnumSet<Direction> connections() {
            return connections;
        }

        long stored() {
            return stored;
        }

        private class ConsumerJournal extends SnapshotJournal<Long> {
            @Override
            protected Long createSnapshot() {
                return stored;
            }

            @Override
            protected void revertToSnapshot(Long snapshot) {
                stored = snapshot;
            }
        }
    }

    static final class Wire implements IWire {
        private final long resistance;
        private final ServerLevel level;
        private final BlockPos pos;
        private final EnumSet<Direction> connections;
        private EUTransferInfo lastLoad = EUTransferInfo.ZERO;

        Wire(long resistance, ServerLevel level, BlockPos pos, Direction... connections) {
            this.resistance = resistance;
            this.level = level;
            this.pos = pos;
            this.connections = EnumSet.noneOf(Direction.class);
            this.connections.addAll(List.of(connections));
        }

        @Override
        public EUTransferInfo getElectricLoad() {
            return lastLoad;
        }

        @Override
        public void applyElectricLoad(EUTransferInfo electricLoad) {
            lastLoad = electricLoad;
        }

        @Override
        public long getResistance() {
            return resistance;
        }

        @Override
        public ServerLevel getComponentLevel() {
            return level;
        }

        @Override
        public BlockPos getPos() {
            return pos;
        }

        @Override
        public EnumSet<Direction> connections() {
            return connections;
        }
    }

    static final class Transformer implements IEnergyTransformer {
        private final VoltageTier tier;
        private final ServerLevel level;
        private final BlockPos pos;
        private final EnumSet<Direction> connections;
        private final long capacity;
        private final TransformerJournal journal = new TransformerJournal();
        private long energy;

        Transformer(VoltageTier tier, long capacity, long energy, ServerLevel level, BlockPos pos,
                    Direction... connections) {
            this.tier = tier;
            this.capacity = capacity;
            this.energy = energy;
            this.level = level;
            this.pos = pos;
            this.connections = EnumSet.noneOf(Direction.class);
            this.connections.addAll(List.of(connections));
        }

        @Override
        public EUTransferInfo extractEnergy(long amount, @Nullable TransactionContext transaction) {
            Util.checkNonNegative(amount);
            long extracted = Math.min(amount, energy);
            if (extracted > 0) {
                if (transaction != null) {
                    journal.updateSnapshots(transaction);
                }
                energy -= extracted;
            }
            return EUTransferInfo.power(tier, extracted);
        }

        @Override
        public VoltageTier getGeneratorVoltageTier() {
            return tier;
        }

        @Override
        public EUTransferInfo insertEU(EUTransferInfo info, @Nullable TransactionContext transaction) {
            long inserted = Math.min(info.power(), capacity - energy);
            if (inserted > 0) {
                if (transaction != null) {
                    journal.updateSnapshots(transaction);
                }
                energy += inserted;
            }
            return EUTransferInfo.power(info.voltageTier(), inserted);
        }

        @Override
        public VoltageTier getInputVoltageTier() {
            return tier;
        }

        @Override
        public VoltageTier getOutputVoltageTier() {
            return tier;
        }

        @Override
        public ServerLevel getComponentLevel() {
            return level;
        }

        @Override
        public BlockPos getPos() {
            return pos;
        }

        @Override
        public EnumSet<Direction> connections() {
            return connections;
        }

        long energy() {
            return energy;
        }

        private class TransformerJournal extends SnapshotJournal<Long> {
            @Override
            protected Long createSnapshot() {
                return energy;
            }

            @Override
            protected void revertToSnapshot(Long snapshot) {
                energy = snapshot;
            }
        }
    }
}
