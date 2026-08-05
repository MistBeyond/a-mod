package com.mistbeyond.examplemod.core.logistic.impl;

import com.mistbeyond.examplemod.core.VoltageTier;
import com.mistbeyond.examplemod.core.logistic.energy.EUTransferInfo;
import com.mistbeyond.examplemod.core.logistic.energy.IEnergyComponent;
import com.mistbeyond.examplemod.core.logistic.impl.EnergyNetworkTestComponents.Consumer;
import com.mistbeyond.examplemod.core.logistic.impl.EnergyNetworkTestComponents.Generator;
import com.mistbeyond.examplemod.core.logistic.impl.EnergyNetworkTestComponents.Transformer;
import com.mistbeyond.examplemod.core.logistic.impl.EnergyNetworkTestComponents.Wire;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EnergyNetworkTopologyTest {

    private static final BlockPos P0 = new BlockPos(0, 64, 0);
    private static final BlockPos P1 = new BlockPos(1, 64, 0);
    private static final BlockPos P2 = new BlockPos(2, 64, 0);
    private static final BlockPos P3 = new BlockPos(2, 64, 1);
    private static final BlockPos P4 = new BlockPos(1, 64, 1);
    private static final BlockPos P5 = new BlockPos(0, 64, 1);

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
    void transformerHeadAndTailInSameNetworkAreDecoupled() {
        Generator source = new Generator(VoltageTier.LOW, 10_000, level, P0, Direction.EAST);
        Wire wireIn = new Wire(1, level, P1, Direction.WEST, Direction.EAST);
        Transformer transformer = new Transformer(VoltageTier.LOW, 10_000, 10_000, level, P2, Direction.WEST, Direction.EAST);
        Wire wireOut = new Wire(2, level, new BlockPos(3, 64, 0), Direction.WEST, Direction.EAST);
        Consumer target = new Consumer(VoltageTier.LOW, 10_000, 0, level, new BlockPos(4, 64, 0), Direction.WEST);
        EnergyNetwork network = newRegisteredNetwork();
        for (IEnergyComponent component : List.of(source, wireIn, transformer, wireOut, target)) {
            network.addComponent(component);
        }

        assertTrue(network.getGenerators().contains(transformer));
        assertTrue(network.getConsumers().contains(transformer));

        EUTransferInfo targetResult = network.pullEnergy(target, EUTransferInfo.power(VoltageTier.LOW, 64), null);
        assertEquals(64, targetResult.power());
        assertEquals(10_000 - 72, transformer.energy());
        assertEquals(64, target.stored());
        assertEquals(10_000, source.energy());

        EUTransferInfo inputResult = network.pullEnergy(transformer, EUTransferInfo.power(VoltageTier.LOW, 64), null);
        assertEquals(64, inputResult.power());
        assertEquals(10_000 - 68, source.energy());
        assertEquals(10_000 - 72 + 64, transformer.energy());
    }

    @Test
    void consumerOnOutputSideCannotReachGeneratorThroughTransformer() {
        Generator source = new Generator(VoltageTier.LOW, 10_000, level, P0, Direction.EAST);
        Wire wireIn = new Wire(1, level, P1, Direction.WEST, Direction.EAST);
        Transformer transformer = new Transformer(VoltageTier.LOW, 10_000, 0, level, P2, Direction.WEST, Direction.EAST);
        Wire wireOut = new Wire(2, level, new BlockPos(3, 64, 0), Direction.WEST, Direction.EAST);
        Consumer target = new Consumer(VoltageTier.LOW, 10_000, 0, level, new BlockPos(4, 64, 0), Direction.WEST);
        EnergyNetwork network = newRegisteredNetwork();
        for (IEnergyComponent component : List.of(source, wireIn, transformer, wireOut, target)) {
            network.addComponent(component);
        }

        EUTransferInfo result = network.pullEnergy(target, EUTransferInfo.power(VoltageTier.LOW, 64), null);

        assertEquals(EUTransferInfo.ZERO, result);
        assertEquals(10_000, source.energy());
        assertEquals(0, transformer.energy());
        assertEquals(0, target.stored());
    }

    @Test
    void transformerReceivesFromInputSideThenSuppliesOutputSide() {
        Generator source = new Generator(VoltageTier.LOW, 10_000, level, P0, Direction.EAST);
        Transformer transformer = new Transformer(VoltageTier.LOW, 10_000, 0, level, P1, Direction.WEST, Direction.EAST);
        Consumer target = new Consumer(VoltageTier.LOW, 10_000, 0, level, P2, Direction.WEST);
        EnergyNetwork network = newRegisteredNetwork();
        network.addComponent(source);
        network.addComponent(transformer);
        network.addComponent(target);

        EUTransferInfo input = network.pullEnergy(transformer, EUTransferInfo.power(VoltageTier.LOW, 64), null);
        assertEquals(64, input.power());
        assertEquals(10_000 - 64, source.energy());
        assertEquals(64, transformer.energy());

        EUTransferInfo output = network.pullEnergy(target, EUTransferInfo.power(VoltageTier.LOW, 64), null);
        assertEquals(64, output.power());
        assertEquals(0, transformer.energy());
        assertEquals(64, target.stored());
    }

    @Test
    void cycleNetworkPicksLowestResistanceRouteWithoutHanging() {
        Generator generator = new Generator(VoltageTier.LOW, 10_000, level, P0, Direction.EAST, Direction.SOUTH);
        Wire wireA = new Wire(1, level, P1, Direction.WEST, Direction.EAST);
        Wire wireB = new Wire(1, level, P2, Direction.WEST, Direction.SOUTH);
        Consumer consumer = new Consumer(VoltageTier.LOW, 10_000, 0, level, P3, Direction.NORTH, Direction.WEST);
        Wire wireC = new Wire(3, level, P4, Direction.WEST, Direction.EAST);
        Wire wireD = new Wire(4, level, P5, Direction.EAST, Direction.NORTH);
        EnergyNetwork network = newRegisteredNetwork();
        for (IEnergyComponent component : List.of(generator, wireA, wireB, consumer, wireC, wireD)) {
            network.addComponent(component);
        }

        EUTransferInfo result = network.pullEnergy(consumer, EUTransferInfo.power(VoltageTier.LOW, 64), null);

        assertEquals(64, result.power());
        assertEquals(10_000 - 72, generator.energy());
        assertEquals(64, consumer.stored());
        assertEquals(72, wireA.getElectricLoad().power());
        assertEquals(72, wireB.getElectricLoad().power());
        assertEquals(EUTransferInfo.ZERO, wireC.getElectricLoad());
        assertEquals(EUTransferInfo.ZERO, wireD.getElectricLoad());
    }

    @Test
    void sameVoltagePrefersLowerResistanceRoute() {
        Generator far = new Generator(VoltageTier.LOW, 10_000, level, P0, Direction.EAST);
        Wire farWire = new Wire(10, level, P1, Direction.WEST, Direction.EAST);
        Consumer consumer = new Consumer(VoltageTier.LOW, 10_000, 0, level, P2, Direction.WEST, Direction.EAST);
        Wire nearWire = new Wire(1, level, new BlockPos(3, 64, 0), Direction.WEST, Direction.EAST);
        Generator near = new Generator(VoltageTier.LOW, 10_000, level, new BlockPos(4, 64, 0), Direction.WEST);
        EnergyNetwork network = newRegisteredNetwork();
        for (IEnergyComponent component : List.of(far, farWire, consumer, nearWire, near)) {
            network.addComponent(component);
        }

        EUTransferInfo result = network.pullEnergy(consumer, EUTransferInfo.power(VoltageTier.LOW, 64), null);

        assertEquals(64, result.power());
        assertEquals(10_000, far.energy());
        assertEquals(10_000 - 68, near.energy());
        assertEquals(64, consumer.stored());
        assertEquals(EUTransferInfo.ZERO, farWire.getElectricLoad());
        assertEquals(68, nearWire.getElectricLoad().power());
    }

    @Test
    void shortfallOnPreferredRouteFallsBackToNextRoute() {
        Generator highShort = new Generator(VoltageTier.HIGH, 50, level, P0, Direction.EAST);
        Wire wire1 = new Wire(10, level, P1, Direction.WEST, Direction.EAST);
        Consumer consumer = new Consumer(VoltageTier.LOW, 10_000, 0, level, P2, Direction.WEST, Direction.EAST);
        Wire wire2 = new Wire(1, level, new BlockPos(3, 64, 0), Direction.WEST, Direction.EAST);
        Generator lowBackup = new Generator(VoltageTier.LOW, 10_000, level, new BlockPos(4, 64, 0), Direction.WEST);
        EnergyNetwork network = newRegisteredNetwork();
        for (IEnergyComponent component : List.of(highShort, wire1, consumer, wire2, lowBackup)) {
            network.addComponent(component);
        }

        EUTransferInfo result = network.pullEnergy(consumer, EUTransferInfo.power(VoltageTier.LOW, 64), null);

        assertEquals(64, result.power());
        assertEquals(50, highShort.energy());
        assertEquals(10_000 - 68, lowBackup.energy());
        assertEquals(64, consumer.stored());
        assertEquals(EUTransferInfo.ZERO, wire1.getElectricLoad());
        assertEquals(68, wire2.getElectricLoad().power());
    }

    @Test
    void multipleConsumersPullSequentiallyAndShortfallRollsBack() {
        Generator generator = new Generator(VoltageTier.LOW, 100, level, P0, Direction.EAST, Direction.SOUTH);
        Consumer first = new Consumer(VoltageTier.LOW, 10_000, 0, level, P1, Direction.WEST);
        Consumer second = new Consumer(VoltageTier.LOW, 10_000, 0, level, P5, Direction.NORTH);
        EnergyNetwork network = newRegisteredNetwork();
        network.addComponent(generator);
        network.addComponent(first);
        network.addComponent(second);

        assertEquals(64, network.pullEnergy(first, EUTransferInfo.power(VoltageTier.LOW, 64), null).power());
        assertEquals(36, generator.energy());
        assertEquals(64, first.stored());

        assertEquals(EUTransferInfo.ZERO, network.pullEnergy(second, EUTransferInfo.power(VoltageTier.LOW, 64), null));
        assertEquals(36, generator.energy());
        assertEquals(0, second.stored());
    }

    @Test
    void addSameComponentTwiceAndRemoveMissingAreNoOps() {
        Consumer consumer = new Consumer(VoltageTier.LOW, 10_000, 0, level, P0);
        EnergyNetwork network = newRegisteredNetwork();

        assertTrue(network.addComponent(consumer));
        assertFalse(network.addComponent(consumer));
        assertEquals(1, network.getConsumers().size());

        Consumer stranger = new Consumer(VoltageTier.LOW, 10_000, 0, level, P1);
        network.removeComponent(stranger);

        assertEquals(1, network.getConsumers().size());
        assertEquals(1, EnergyNetworkManager.INSTANCE.getNetworks(level).size());
    }

    @Test
    void zeroRequestDoesNotTouchNetwork() {
        Generator generator = new Generator(VoltageTier.LOW, 10_000, level, P0, Direction.EAST);
        Consumer consumer = new Consumer(VoltageTier.LOW, 10_000, 0, level, P1, Direction.WEST);
        EnergyNetwork network = newRegisteredNetwork();
        network.addComponent(generator);
        network.addComponent(consumer);

        EUTransferInfo result = network.pullEnergy(consumer, EUTransferInfo.ZERO, null);

        assertEquals(EUTransferInfo.ZERO, result);
        assertEquals(10_000, generator.energy());
        assertEquals(0, consumer.stored());
    }

    private EnergyNetwork newRegisteredNetwork() {
        EnergyNetwork network = new EnergyNetwork(level);
        EnergyNetworkManager.INSTANCE.register(network);
        return network;
    }
}
