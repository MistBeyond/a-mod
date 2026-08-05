package com.mistbeyond.examplemod.core.logistic.impl;

import com.mistbeyond.examplemod.core.VoltageTier;
import com.mistbeyond.examplemod.core.logistic.energy.*;
import com.mistbeyond.examplemod.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EnergyNetworkTest {

    private static final BlockPos P0 = new BlockPos(0, 64, 0);
    private static final BlockPos P1 = new BlockPos(1, 64, 0);
    private static final BlockPos P2 = new BlockPos(2, 64, 0);
    private static final BlockPos P3 = new BlockPos(3, 64, 0);
    private static final BlockPos P4 = new BlockPos(4, 64, 0);

    private ServerLevel level;

    @BeforeEach
    void setUp() {
        level = mock(ServerLevel.class);
        when(level.dimension()).thenReturn(Level.OVERWORLD);
    }

    @AfterEach
    void tearDown() {
        EnergyNetworkManager.INSTANCE.clearAll();
    }

    @Test
    void pullEnergyAppliesLineLossOnGeneratorSide() {
        StubGenerator generator = new StubGenerator(VoltageTier.LOW, 10_000, level, P0, Direction.EAST);
        StubWire wire = new StubWire(4, level, P1, Direction.WEST, Direction.EAST);
        StubConsumer consumer = new StubConsumer(VoltageTier.LOW, 10_000, 0, level, P2, Direction.WEST);
        EnergyNetwork network = newRegisteredNetwork();
        network.addComponent(generator);
        network.addComponent(wire);
        network.addComponent(consumer);

        EUTransferInfo result = network.pullEnergy(consumer, EUTransferInfo.power(VoltageTier.LOW, 64), null);

        assertEquals(64, result.power());
        assertEquals(10_000 - 80, generator.energy());
        assertEquals(64, consumer.stored());
        assertEquals(80, wire.getElectricLoad().power());
        assertEquals(VoltageTier.LOW, wire.getElectricLoad().voltageTier());
    }

    @Test
    void pullEnergyFallsThroughToNextGeneratorWhenPreferredOneIsEmpty() {
        StubGenerator emptyHigh = new StubGenerator(VoltageTier.HIGH, 0, level, P0, Direction.EAST);
        StubWire wire1 = new StubWire(1, level, P1, Direction.WEST, Direction.EAST);
        StubConsumer consumer = new StubConsumer(VoltageTier.LOW, 10_000, 0, level, P2, Direction.WEST, Direction.EAST);
        StubWire wire2 = new StubWire(2, level, P3, Direction.WEST, Direction.EAST);
        StubGenerator backupLow = new StubGenerator(VoltageTier.LOW, 10_000, level, P4, Direction.WEST);
        EnergyNetwork network = newRegisteredNetwork();
        for (IEnergyComponent component : List.of(emptyHigh, wire1, consumer, wire2, backupLow)) {
            network.addComponent(component);
        }

        EUTransferInfo result = network.pullEnergy(consumer, EUTransferInfo.power(VoltageTier.LOW, 64), null);

        assertEquals(64, result.power());
        assertEquals(0, emptyHigh.energy());
        assertEquals(10_000 - 72, backupLow.energy());
        assertEquals(64, consumer.stored());
        assertEquals(EUTransferInfo.ZERO, wire1.getElectricLoad());
        assertEquals(72, wire2.getElectricLoad().power());
    }

    @Test
    void pullEnergyPrefersHigherVoltageGenerator() {
        StubGenerator low = new StubGenerator(VoltageTier.LOW, 10_000, level, P0, Direction.EAST);
        StubWire wire1 = new StubWire(10, level, P1, Direction.WEST, Direction.EAST);
        StubConsumer consumer = new StubConsumer(VoltageTier.LOW, 10_000, 0, level, P2, Direction.WEST, Direction.EAST);
        StubWire wire2 = new StubWire(10, level, P3, Direction.WEST, Direction.EAST);
        StubGenerator high = new StubGenerator(VoltageTier.HIGH, 10_000, level, P4, Direction.WEST);
        EnergyNetwork network = newRegisteredNetwork();
        for (IEnergyComponent component : List.of(low, wire1, consumer, wire2, high)) {
            network.addComponent(component);
        }

        EUTransferInfo result = network.pullEnergy(consumer, EUTransferInfo.power(VoltageTier.LOW, 64), null);

        assertEquals(64, result.power());
        assertEquals(10_000, low.energy());
        assertEquals(10_000 - 64, high.energy());
        assertEquals(64, consumer.stored());
        assertEquals(64, wire2.getElectricLoad().power());
    }

    @Test
    void pullEnergyPrefersNonTransformerGenerator() {
        StubTransformer transformer = new StubTransformer(VoltageTier.LOW, 10_000, level, P0, Direction.EAST);
        StubWire wire1 = new StubWire(1, level, P1, Direction.WEST, Direction.EAST);
        StubConsumer consumer = new StubConsumer(VoltageTier.LOW, 10_000, 0, level, P2, Direction.WEST, Direction.EAST);
        StubWire wire2 = new StubWire(2, level, P3, Direction.WEST, Direction.EAST);
        StubGenerator plain = new StubGenerator(VoltageTier.LOW, 10_000, level, P4, Direction.WEST);
        EnergyNetwork network = newRegisteredNetwork();
        for (IEnergyComponent component : List.of(transformer, wire1, consumer, wire2, plain)) {
            network.addComponent(component);
        }

        EUTransferInfo result = network.pullEnergy(consumer, EUTransferInfo.power(VoltageTier.LOW, 64), null);

        assertEquals(64, result.power());
        assertEquals(10_000, transformer.energy());
        assertEquals(10_000 - 72, plain.energy());
        assertEquals(64, consumer.stored());
    }

    @Test
    void pullEnergyReturnsZeroAndRollsBackWhenGeneratorCannotCoverRequest() {
        StubGenerator generator = new StubGenerator(VoltageTier.LOW, 50, level, P0, Direction.EAST);
        StubWire wire = new StubWire(4, level, P1, Direction.WEST, Direction.EAST);
        StubConsumer consumer = new StubConsumer(VoltageTier.LOW, 10_000, 0, level, P2, Direction.WEST);
        EnergyNetwork network = newRegisteredNetwork();
        network.addComponent(generator);
        network.addComponent(wire);
        network.addComponent(consumer);

        EUTransferInfo result = network.pullEnergy(consumer, EUTransferInfo.power(VoltageTier.LOW, 64), null);

        assertEquals(EUTransferInfo.ZERO, result);
        assertEquals(50, generator.energy());
        assertEquals(0, consumer.stored());
        assertEquals(EUTransferInfo.ZERO, wire.getElectricLoad());
    }

    @Test
    void pullEnergyReturnsZeroAndRollsBackWhenConsumerIsFull() {
        StubGenerator generator = new StubGenerator(VoltageTier.LOW, 10_000, level, P0, Direction.EAST);
        StubWire wire = new StubWire(4, level, P1, Direction.WEST, Direction.EAST);
        StubConsumer consumer = new StubConsumer(VoltageTier.LOW, 64, 64, level, P2, Direction.WEST);
        EnergyNetwork network = newRegisteredNetwork();
        network.addComponent(generator);
        network.addComponent(wire);
        network.addComponent(consumer);

        EUTransferInfo result = network.pullEnergy(consumer, EUTransferInfo.power(VoltageTier.LOW, 64), null);

        assertEquals(EUTransferInfo.ZERO, result);
        assertEquals(10_000, generator.energy());
        assertEquals(64, consumer.stored());
        assertEquals(EUTransferInfo.ZERO, wire.getElectricLoad());
    }

    @Test
    void pullEnergyReturnsZeroWhenConsumerIsNotConnectedToAnyGenerator() {
        StubConsumer consumer = new StubConsumer(VoltageTier.LOW, 10_000, 0, level, P0);
        EnergyNetwork network = newRegisteredNetwork();
        network.addComponent(consumer);

        EUTransferInfo result = network.pullEnergy(consumer, EUTransferInfo.power(VoltageTier.LOW, 64), null);

        assertEquals(EUTransferInfo.ZERO, result);
        assertEquals(0, consumer.stored());
    }

    @Test
    void removeComponentSplitsNetworkIntoConnectedParts() {
        StubGenerator generator = new StubGenerator(VoltageTier.LOW, 10_000, level, P0, Direction.EAST);
        StubWire wire1 = new StubWire(1, level, P1, Direction.WEST, Direction.EAST);
        StubWire wire2 = new StubWire(1, level, P2, Direction.WEST, Direction.EAST);
        StubConsumer consumer = new StubConsumer(VoltageTier.LOW, 10_000, 0, level, P3, Direction.WEST);
        EnergyNetwork network = newRegisteredNetwork();
        for (IEnergyComponent component : List.of(generator, wire1, wire2, consumer)) {
            network.addComponent(component);
        }

        network.removeComponent(wire1);

        Collection<IEnergyNetwork> networks = EnergyNetworkManager.INSTANCE.getNetworks(level);
        assertEquals(2, networks.size());
        assertEquals(1, networks.stream().mapToLong(n -> n.getGenerators().size()).sum());
        assertEquals(1, networks.stream().mapToLong(n -> n.getConsumers().size()).sum());
        assertEquals(1, networks.stream().filter(n -> !n.getGenerators().isEmpty()).count());
        assertEquals(1, networks.stream().filter(n -> !n.getConsumers().isEmpty()).count());
        assertTrue(networks.stream().noneMatch(n -> n.isNetworkAvailableAt(P1)));
        assertTrue(networks.stream().anyMatch(n -> n.isNetworkAvailableAt(P0)));
        assertTrue(networks.stream().anyMatch(n -> n.isNetworkAvailableAt(P2) && n.isNetworkAvailableAt(P3)));
    }

    @Test
    void removeComponentUnregistersEmptyNetwork() {
        StubConsumer consumer = new StubConsumer(VoltageTier.LOW, 10_000, 0, level, P0);
        EnergyNetwork network = newRegisteredNetwork();
        network.addComponent(consumer);
        assertEquals(1, EnergyNetworkManager.INSTANCE.getNetworks(level).size());

        network.removeComponent(consumer);

        assertTrue(EnergyNetworkManager.INSTANCE.getNetworks(level).isEmpty());
    }

    @Test
    void removeEndpointKeepsNetworkAlive() {
        StubGenerator generator = new StubGenerator(VoltageTier.LOW, 10_000, level, P0, Direction.EAST);
        StubWire wire = new StubWire(4, level, P1, Direction.WEST, Direction.EAST);
        StubConsumer consumer = new StubConsumer(VoltageTier.LOW, 10_000, 0, level, P2, Direction.WEST);
        EnergyNetwork network = newRegisteredNetwork();
        for (IEnergyComponent component : List.of(generator, wire, consumer)) {
            network.addComponent(component);
        }

        network.removeComponent(generator);

        Collection<IEnergyNetwork> networks = EnergyNetworkManager.INSTANCE.getNetworks(level);
        assertEquals(1, networks.size());
        IEnergyNetwork remaining = networks.iterator().next();
        assertTrue(remaining.getConsumers().contains(consumer));
        assertTrue(remaining.getGenerators().isEmpty());
        assertTrue(remaining.isNetworkAvailableAt(P1));
        assertTrue(remaining.isNetworkAvailableAt(P2));
        assertFalse(remaining.isNetworkAvailableAt(P0));
    }

    private EnergyNetwork newRegisteredNetwork() {
        EnergyNetwork network = new EnergyNetwork(level);
        EnergyNetworkManager.INSTANCE.register(network);
        return network;
    }

    private static final class StubGenerator implements IEnergyGenerator {
        private final VoltageTier tier;
        private final ServerLevel level;
        private final BlockPos pos;
        private final EnumSet<Direction> connections;
        private final GeneratorJournal journal = new GeneratorJournal();
        private long energy;

        private StubGenerator(VoltageTier tier, long energy, ServerLevel level, BlockPos pos, Direction... connections) {
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

        @Override
        public boolean canConnectTo(BlockGetter level, BlockPos pos, Direction direction) {
            return connections().contains(direction);
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

    private static final class StubConsumer implements IEnergyConsumer {
        private final VoltageTier tier;
        private final ServerLevel level;
        private final BlockPos pos;
        private final EnumSet<Direction> connections;
        private final long capacity;
        private final ConsumerJournal journal = new ConsumerJournal();
        private long stored;

        private StubConsumer(VoltageTier tier, long capacity, long stored, ServerLevel level, BlockPos pos,
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

        @Override
        public boolean canConnectTo(BlockGetter level, BlockPos pos, Direction direction) {
            return connections().contains(direction);
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

    private static final class StubWire implements IWire {
        private final long resistance;
        private final ServerLevel level;
        private final BlockPos pos;
        private final EnumSet<Direction> connections;
        private EUTransferInfo lastLoad = EUTransferInfo.ZERO;

        private StubWire(long resistance, ServerLevel level, BlockPos pos, Direction... connections) {
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
        public void meltdown() {
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

        @Override
        public boolean canConnectTo(BlockGetter level, BlockPos pos, Direction direction) {
            return connections().contains(direction);
        }
    }

    private static final class StubTransformer implements IEnergyTransformer {
        private final VoltageTier tier;
        private final ServerLevel level;
        private final BlockPos pos;
        private final EnumSet<Direction> connections;
        private final TransformerJournal journal = new TransformerJournal();
        private long energy;

        private StubTransformer(VoltageTier tier, long energy, ServerLevel level, BlockPos pos, Direction... connections) {
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
        public VoltageTier getInputVoltageTier() {
            return tier;
        }

        @Override
        public VoltageTier getOutputVoltageTier() {
            return tier;
        }

        @Override
        public EUTransferInfo insertEU(EUTransferInfo info, @Nullable TransactionContext transaction) {
            // The consumer side is not used by these tests.
            return EUTransferInfo.ZERO;
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

        @Override
        public boolean canConnectTo(BlockGetter level, BlockPos pos, Direction direction) {
            return connections().contains(direction);
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
