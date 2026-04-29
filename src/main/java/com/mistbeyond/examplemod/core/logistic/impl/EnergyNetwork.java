package com.mistbeyond.examplemod.core.logistic.impl;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.mistbeyond.examplemod.core.VoltageTier;
import com.mistbeyond.examplemod.core.logistic.energy.*;
import net.minecraft.server.level.ServerLevel;

import java.util.List;
import java.util.SequencedSet;
import java.util.Set;

public class EnergyNetwork implements IEnergyNetwork {
    private final ServerLevel level;
    private final MutableGraph<IEnergyNetworkComponent> componentGraph = GraphBuilder.directed().build();
    private Cache cache;

    public EnergyNetwork(ServerLevel level) {
        this.level = level;
    }

    @Override
    public Set<IEnergyGenerator> getGenerators() {
        return Set.copyOf(cache.generators);
    }

    @Override
    public Set<IEnergyConsumer> getConsumers() {
        return Set.copyOf(cache.consumers);
    }

    @Override
    public ServerLevel getNetworkLevel() {
        return level;
    }

    @Override
    public void requestEnergy(IEnergyConsumer energyConsumer, EUTransferInfo info) {

    }

    @Override
    public void cancelRequestEnergy(IEnergyConsumer energyConsumer, EUTransferInfo info) {
    }

    @Override
    public void onComponentStateChanged(IEnergyNetworkComponent component) {
//        this.cache = Cache.of(componentGraph);
    }

    private record Cache(SequencedSet<IEnergyGenerator> generators, SequencedSet<IEnergyConsumer> consumers,
                         Set<IWire> cables, VoltageTier currentMaxVoltage, List<GeneratorRoute> energyNetwork) {
//        private static final Cache EMPTY = new Cache(Set.of(), Set.of(), Set.of());

//        private static Cache of(MutableGraph<IEnergyNetworkComponent> graph) {
//
//            var sortedGenerators = graph.nodes().stream()
//                    .filter(IEnergyGenerator.class::isInstance)
//                    .map(IEnergyGenerator.class::cast)
//                    .sorted(Comparator.comparingLong(g -> g.getGeneratorVoltageTier().value))
//                    .toList()
//                    .reversed();
//            Graphs.inducedSubgraph();
//            return new Cache();
//        }
    }

    private record GeneratorRoute(IEnergyGenerator generator,
                                  long totalResistance) implements Comparable<GeneratorRoute> {
        @Override
        public int compareTo(EnergyNetwork.GeneratorRoute o) {
            VoltageTier v1 = generator.getGeneratorVoltageTier(), v2 = o.generator.getGeneratorVoltageTier();
            if (v1 != v2) {
                return Long.compare(v1.value, v2.value);
            }
            return Long.compare(totalResistance, o.totalResistance);
        }
    }
}
