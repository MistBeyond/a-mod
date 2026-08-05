package com.mistbeyond.examplemod.core.logistic.impl;

import com.mistbeyond.examplemod.core.VoltageTier;
import com.mistbeyond.examplemod.core.logistic.energy.*;
import com.mistbeyond.examplemod.core.logistic.impl.EnergyNetworkTestComponents.Consumer;
import com.mistbeyond.examplemod.core.logistic.impl.EnergyNetworkTestComponents.Generator;
import com.mistbeyond.examplemod.core.logistic.impl.EnergyNetworkTestComponents.Wire;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class EnergyNetworkManagerTest {

    private static final BlockPos P0 = new BlockPos(0, 64, 0);
    private static final BlockPos P1 = new BlockPos(1, 64, 0);
    private static final BlockPos P2 = new BlockPos(2, 64, 0);
    private static final BlockPos P3 = new BlockPos(3, 64, 0);

    private ServerLevel level;

    @BeforeEach
    void setUp() {
        level = EnergyNetworkTestComponents.mockLevel();
    }

    @AfterEach
    void tearDown() {
        EnergyNetworkManager.INSTANCE.clearAll();
    }

    @Test
    void componentLoadMergesComponentsIntoOneNetworkOnNextTick() {
        Generator rawGenerator = new Generator(VoltageTier.LOW, 10_000, level, P0, Direction.EAST);
        IEnergyGenerator generator = EnergyNetworkTestComponents.wrap(rawGenerator);
        Wire rawWire = new Wire(4, level, P1, Direction.WEST, Direction.EAST);
        IWire wire = EnergyNetworkTestComponents.wrap(rawWire);
        Consumer rawConsumer = new Consumer(VoltageTier.LOW, 10_000, 0, level, P2, Direction.WEST);
        IEnergyConsumer consumer = EnergyNetworkTestComponents.wrap(rawConsumer);
        EnergyNetworkTestComponents.mapBlockEntities(level, generator, wire, consumer);

        EnergyNetworkManager.INSTANCE.onComponentLoaded(generator);
        EnergyNetworkManager.INSTANCE.onComponentLoaded(wire);
        EnergyNetworkManager.INSTANCE.onComponentLoaded(consumer);
        assertTrue(EnergyNetworkManager.INSTANCE.getNetworks(level).isEmpty());

        EnergyNetworkManager.INSTANCE.processPendingLoads(level);

        Collection<IEnergyNetwork> networks = EnergyNetworkManager.INSTANCE.getNetworks(level);
        assertEquals(1, networks.size());
        IEnergyNetwork network = networks.iterator().next();
        assertTrue(network.getGenerators().contains(generator));
        assertTrue(network.getConsumers().contains(consumer));

        EUTransferInfo result = network.pullEnergy(consumer, EUTransferInfo.power(VoltageTier.LOW, 64), null);

        assertEquals(64, result.power());
        assertEquals(10_000 - 80, rawGenerator.energy());
        assertEquals(64, rawConsumer.stored());
    }

    @Test
    void componentsLoadedAcrossChunksMergeIntoOneNetwork() {
        Generator rawGenerator = new Generator(VoltageTier.LOW, 10_000, level, P0, Direction.EAST);
        IEnergyGenerator generator = EnergyNetworkTestComponents.wrap(rawGenerator);
        Wire rawWire1 = new Wire(4, level, P1, Direction.WEST, Direction.EAST);
        IWire wire1 = EnergyNetworkTestComponents.wrap(rawWire1);
        EnergyNetworkTestComponents.mapBlockEntities(level, generator, wire1);
        loadAll(generator, wire1);
        assertEquals(1, EnergyNetworkManager.INSTANCE.getNetworks(level).size());

        Wire rawWire2 = new Wire(2, level, P2, Direction.WEST, Direction.EAST);
        IWire wire2 = EnergyNetworkTestComponents.wrap(rawWire2);
        Consumer rawConsumer = new Consumer(VoltageTier.LOW, 10_000, 0, level, P3, Direction.WEST);
        IEnergyConsumer consumer = EnergyNetworkTestComponents.wrap(rawConsumer);
        EnergyNetworkTestComponents.mapBlockEntities(level, wire2, consumer);
        loadAll(wire2, consumer);

        Collection<IEnergyNetwork> networks = EnergyNetworkManager.INSTANCE.getNetworks(level);
        assertEquals(1, networks.size());
        IEnergyNetwork network = networks.iterator().next();
        assertTrue(network.getGenerators().contains(generator));
        assertTrue(network.getConsumers().contains(consumer));

        EUTransferInfo result = network.pullEnergy(consumer, EUTransferInfo.power(VoltageTier.LOW, 64), null);

        assertEquals(64, result.power());
        assertEquals(10_000 - 88, rawGenerator.energy());
        assertEquals(64, rawConsumer.stored());
    }

    @Test
    void componentRemovalThroughManagerSplitsNetwork() {
        Generator rawGenerator = new Generator(VoltageTier.LOW, 10_000, level, P0, Direction.EAST);
        IEnergyGenerator generator = EnergyNetworkTestComponents.wrap(rawGenerator);
        Wire rawWire1 = new Wire(1, level, P1, Direction.WEST, Direction.EAST);
        IWire wire1 = EnergyNetworkTestComponents.wrap(rawWire1);
        Wire rawWire2 = new Wire(1, level, P2, Direction.WEST, Direction.EAST);
        IWire wire2 = EnergyNetworkTestComponents.wrap(rawWire2);
        Consumer rawConsumer = new Consumer(VoltageTier.LOW, 10_000, 0, level, P3, Direction.WEST);
        IEnergyConsumer consumer = EnergyNetworkTestComponents.wrap(rawConsumer);
        EnergyNetworkTestComponents.mapBlockEntities(level, generator, wire1, wire2, consumer);
        loadAll(generator, wire1, wire2, consumer);
        assertEquals(1, EnergyNetworkManager.INSTANCE.getNetworks(level).size());

        EnergyNetworkManager.INSTANCE.onComponentRemoved(wire1);

        Collection<IEnergyNetwork> networks = EnergyNetworkManager.INSTANCE.getNetworks(level);
        assertEquals(2, networks.size());
        assertEquals(1, networks.stream().filter(n -> !n.getGenerators().isEmpty()).count());
        assertEquals(1, networks.stream().filter(n -> !n.getConsumers().isEmpty()).count());
        assertTrue(EnergyNetworkManager.INSTANCE.getNetworksAt(level, P1).isEmpty());
    }

    @Test
    void pendingComponentRemovedBeforeTickDoesNotJoinNetwork() {
        Consumer rawConsumer = new Consumer(VoltageTier.LOW, 10_000, 0, level, P0);
        IEnergyConsumer consumer = EnergyNetworkTestComponents.wrap(rawConsumer);
        EnergyNetworkTestComponents.mapBlockEntities(level, consumer);

        EnergyNetworkManager.INSTANCE.onComponentLoaded(consumer);
        EnergyNetworkManager.INSTANCE.onComponentRemoved(consumer);
        EnergyNetworkManager.INSTANCE.processPendingLoads(level);

        assertTrue(EnergyNetworkManager.INSTANCE.getNetworks(level).isEmpty());
    }

    @Test
    void mergeNetworkAtNonComponentPositionThrows() {
        assertThrows(IllegalArgumentException.class, () -> EnergyNetworkManager.INSTANCE.mergeNetwork(level, P2));
    }

    @Test
    void mergeNetworkReturnsExistingNetworkForAlreadyMergedComponent() {
        Consumer rawConsumer = new Consumer(VoltageTier.LOW, 10_000, 0, level, P0);
        IEnergyConsumer consumer = EnergyNetworkTestComponents.wrap(rawConsumer);
        EnergyNetworkTestComponents.mapBlockEntities(level, consumer);

        IEnergyNetwork first = EnergyNetworkManager.INSTANCE.mergeNetwork(level, P0);
        IEnergyNetwork again = EnergyNetworkManager.INSTANCE.mergeNetwork(level, P0);

        assertSame(first, again);
        assertEquals(1, EnergyNetworkManager.INSTANCE.getNetworks(level).size());
    }

    @Test
    void levelUnloadClearsOnlyThatDimension() {
        ServerLevel nether = EnergyNetworkTestComponents.mockLevel();
        when(nether.dimension()).thenReturn(Level.NETHER);
        Consumer rawOverworldConsumer = new Consumer(VoltageTier.LOW, 10_000, 0, level, P0);
        IEnergyConsumer overworldConsumer = EnergyNetworkTestComponents.wrap(rawOverworldConsumer);
        Consumer rawNetherConsumer = new Consumer(VoltageTier.LOW, 10_000, 0, nether, P0);
        IEnergyConsumer netherConsumer = EnergyNetworkTestComponents.wrap(rawNetherConsumer);
        EnergyNetworkTestComponents.mapBlockEntities(level, overworldConsumer);
        EnergyNetworkTestComponents.mapBlockEntities(nether, netherConsumer);
        EnergyNetworkManager.INSTANCE.onComponentLoaded(overworldConsumer);
        EnergyNetworkManager.INSTANCE.onComponentLoaded(netherConsumer);
        EnergyNetworkManager.INSTANCE.processPendingLoads(level);
        EnergyNetworkManager.INSTANCE.processPendingLoads(nether);
        assertEquals(1, EnergyNetworkManager.INSTANCE.getNetworks(level).size());
        assertEquals(1, EnergyNetworkManager.INSTANCE.getNetworks(nether).size());

        EnergyNetworkManager.INSTANCE.onLevelUnload(level);

        assertTrue(EnergyNetworkManager.INSTANCE.getNetworks(level).isEmpty());
        assertEquals(1, EnergyNetworkManager.INSTANCE.getNetworks(nether).size());
        EnergyNetworkManager.INSTANCE.clearAll();
        assertTrue(EnergyNetworkManager.INSTANCE.getNetworks(nether).isEmpty());
    }

    @Test
    void worldReloadAfterUnloadRebuildsNetwork() {
        Generator rawGenerator = new Generator(VoltageTier.LOW, 10_000, level, P0, Direction.EAST);
        IEnergyGenerator generator = EnergyNetworkTestComponents.wrap(rawGenerator);
        Wire rawWire = new Wire(4, level, P1, Direction.WEST, Direction.EAST);
        IWire wire = EnergyNetworkTestComponents.wrap(rawWire);
        Consumer rawConsumer = new Consumer(VoltageTier.LOW, 10_000, 0, level, P2, Direction.WEST);
        IEnergyConsumer consumer = EnergyNetworkTestComponents.wrap(rawConsumer);
        EnergyNetworkTestComponents.mapBlockEntities(level, generator, wire, consumer);
        loadAll(generator, wire, consumer);
        assertEquals(1, EnergyNetworkManager.INSTANCE.getNetworks(level).size());

        EnergyNetworkManager.INSTANCE.onLevelUnload(level);
        assertTrue(EnergyNetworkManager.INSTANCE.getNetworks(level).isEmpty());

        loadAll(generator, wire, consumer);

        Collection<IEnergyNetwork> networks = EnergyNetworkManager.INSTANCE.getNetworks(level);
        assertEquals(1, networks.size());
        IEnergyNetwork network = networks.iterator().next();
        EUTransferInfo result = network.pullEnergy(consumer, EUTransferInfo.power(VoltageTier.LOW, 64), null);
        assertEquals(64, result.power());
        assertEquals(10_000 - 80, rawGenerator.energy());
    }

    @Test
    void removingLastComponentThroughManagerUnregistersNetwork() {
        Consumer rawConsumer = new Consumer(VoltageTier.LOW, 10_000, 0, level, P0);
        IEnergyConsumer consumer = EnergyNetworkTestComponents.wrap(rawConsumer);
        EnergyNetworkTestComponents.mapBlockEntities(level, consumer);
        EnergyNetworkManager.INSTANCE.onComponentLoaded(consumer);
        EnergyNetworkManager.INSTANCE.processPendingLoads(level);
        assertEquals(1, EnergyNetworkManager.INSTANCE.getNetworks(level).size());

        EnergyNetworkManager.INSTANCE.onComponentRemoved(consumer);

        assertTrue(EnergyNetworkManager.INSTANCE.getNetworks(level).isEmpty());
    }

    private void loadAll(IEnergyComponent... components) {
        for (IEnergyComponent component : components) {
            EnergyNetworkManager.INSTANCE.onComponentLoaded(component);
        }
        EnergyNetworkManager.INSTANCE.processPendingLoads(level);
    }
}
