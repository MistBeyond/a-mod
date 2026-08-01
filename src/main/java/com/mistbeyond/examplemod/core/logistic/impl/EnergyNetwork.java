package com.mistbeyond.examplemod.core.logistic.impl;

import com.google.common.collect.*;
import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.mistbeyond.examplemod.core.VoltageTier;
import com.mistbeyond.examplemod.core.logistic.energy.*;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMaps;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

@Slf4j
public class EnergyNetwork implements IEnergyNetwork {
    private static final Direction[] DIRECTIONS = Direction.values();
    private final ServerLevel level;
    private final MutableGraph<IEnergyComponent> componentGraph = GraphBuilder.undirected().build();
    private final Cache cache = Cache.newEmpty();

    public EnergyNetwork(ServerLevel level) {
        this.level = level;
    }

    private static void notImplemented() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public Set<IEnergyGenerator> getGenerators() {
        return cache.generatorView;
    }

    @Override
    public Set<IEnergyConsumer> getConsumers() {
        return cache.consumerView;
    }

    @Override
    public ServerLevel getNetworkLevel() {
        return level;
    }

    @Override
    public void requestEnergy(IEnergyConsumer energyConsumer, EUTransferInfo info) {
        notImplemented();
    }

    @Override
    public void cancelRequestEnergy(IEnergyConsumer energyConsumer, EUTransferInfo info) {
        notImplemented();
    }

    @Override
    public void onComponentConnectionChanged(IEnergyComponent component) {
        onComponentConnectionChanged(component, false);
    }

    @Override
    public boolean addComponent(IEnergyComponent component) {
        return addComponent(component, false);
    }

    @Override
    public void removeComponent(IEnergyComponent component) {
        if (componentGraph.removeNode(component)) {
            onComponentModified();
        }
    }

    @Override
    public boolean isNetworkAvailableAt(BlockPos pos) {
        return cache.pos2ComponentView.containsKey(pos.asLong());
    }

    Set<IEnergyNetwork> startMergeFrom(IEnergyComponent startComponent) {
        var visiting = new ArrayDeque<IEnergyComponent>();
        var newComponents = new HashSet<IEnergyComponent>();
        var mergedNetworks = new HashSet<IEnergyNetwork>();
        visiting.addLast(startComponent);
        newComponents.add(startComponent);
        while (!visiting.isEmpty()) {
            var curr = visiting.removeFirst();
            for (Direction connection : curr.connections()) {
                if (level.getBlockEntity(curr.getPos().relative(connection)) instanceof IEnergyComponent comp
                        && !componentGraph.nodes().contains(comp) && !newComponents.contains(comp)
                        && comp.isConnectWith(curr)) {
                    visiting.addLast(comp);
                    newComponents.add(comp);
                    mergedNetworks.addAll(comp.getNetwork());
                }
            }
        }
        mergedNetworks.remove(this);
        newComponents.forEach(c -> this.addComponent(c, true));
        onComponentModified();
        return mergedNetworks;
    }

    void onComponentConnectionChanged(IEnergyComponent component, boolean deferredCacheRebuild) {
        if (!componentGraph.nodes().contains(component)) {
            log.warn("Node {} is not exist", component);
            return;
        }
        Set.copyOf(componentGraph.adjacentNodes(component))
                .forEach(node -> componentGraph.removeEdge(node, component));

        for (IEnergyComponent c : cache.adjacentComponentView.get(component)) {
            if (c.isConnectWith(component)) {
                componentGraph.putEdge(component, c);
            }
        }

        if (!deferredCacheRebuild) {
            onComponentModified();
        }
    }

    boolean addComponent(IEnergyComponent component, boolean deferredCacheRebuild) {
        boolean added = componentGraph.addNode(component);
        if (added) {
            onComponentConnectionChanged(component, deferredCacheRebuild);
        }
        return added;
    }

    private void onComponentModified() {
        cache.updateFully(componentGraph);
    }


    private enum Flag implements IEnergyConsumer {
        ROOT;

        private static void notSupport() {
            throw new UnsupportedOperationException("Flag is not support this method");
        }

        @Override
        public EUTransferInfo insertEU() {
            notSupport();
            return null;
        }

        @Override
        public VoltageTier getVoltageTier() {
            notSupport();
            return null;
        }

        @Override
        public ServerLevel getComponentLevel() {
            notSupport();
            return null;
        }

        @Override
        public BlockPos getPos() {
            notSupport();
            return null;
        }

        @Override
        public EnumSet<Direction> connections() {
            notSupport();
            return null;
        }
    }

    private record GeneratorRoute(IEnergyGenerator generator,
                                  long totalResistance,
                                  @Unmodifiable List<IWire> path) {
        public static final Comparator<GeneratorRoute> COMPARATOR =
                Comparator.comparing((GeneratorRoute r) -> r.generator.isTransformer(), Boolean::compare)
                        .thenComparing(r -> r.generator.getGeneratorVoltageTier().value, ((Comparator<Long>) Long::compare).reversed())
                        .thenComparingLong(GeneratorRoute::totalResistance)
                        .thenComparingInt(r -> r.path.size());

        private GeneratorRoute(IEnergyGenerator generator, long totalResistance, @Unmodifiable List<IWire> path) {
            this.generator = generator;
            this.totalResistance = totalResistance;
            this.path = List.copyOf(path);
        }
    }

    /**
     * Do not modify the collections in the cache.
     */
    // 不知道之前在哪里看到的, BlockPos或者说vec3i的哈希函数写的是一坨狗屎, 会导致大量哈希碰撞, 因此这里使用BlockPos#asLong存储
    private static class Cache {
        private @UnmodifiableView Set<IEnergyGenerator> generatorView;
        private @UnmodifiableView Set<IEnergyConsumer> consumerView;
        private @UnmodifiableView Set<IWire> wireView;
        private @UnmodifiableView ListMultimap<IEnergyConsumer, GeneratorRoute> energyNetworkView;
        private @UnmodifiableView Multimap<IEnergyComponent, IEnergyComponent> adjacentComponentView;
        private @UnmodifiableView Long2ReferenceMap<IEnergyComponent> pos2ComponentView;
        private HashSet<IEnergyGenerator> generators;
        private HashSet<IEnergyConsumer> consumers;
        private HashSet<IWire> wires;
        private ListMultimap<IEnergyConsumer, GeneratorRoute> energyNetwork;
        private HashMultimap<IEnergyComponent, IEnergyComponent> adjacentComponent;
        private Long2ReferenceMap<IEnergyComponent> pos2Component;

        private HashMultimap<IEnergyGenerator, GeneratorRoute> generator2Route;
        private HashMultimap<IWire, GeneratorRoute> wire2Route;

        private Cache(HashSet<IEnergyGenerator> generators, HashSet<IEnergyConsumer> consumers, HashSet<IWire> wires, ListMultimap<IEnergyConsumer, GeneratorRoute> energyNetwork, HashMultimap<IEnergyComponent, IEnergyComponent> adjacentComponent, Long2ReferenceOpenHashMap<IEnergyComponent> pos2Component, HashMultimap<IEnergyGenerator, GeneratorRoute> generator2Route, HashMultimap<IWire, GeneratorRoute> wire2Route) {
            this.generators = generators;
            this.consumers = consumers;
            this.wires = wires;
            this.energyNetwork = energyNetwork;
            this.adjacentComponent = adjacentComponent;
            this.pos2Component = pos2Component;
            this.generator2Route = generator2Route;
            this.wire2Route = wire2Route;
            rebuildAllViews();
        }

        public static Cache newEmpty() {
            return new Cache(new HashSet<>(), new HashSet<>(), new HashSet<>(), MultimapBuilder.hashKeys().arrayListValues().build(), HashMultimap.create(), new Long2ReferenceOpenHashMap<>(), HashMultimap.create(), HashMultimap.create());
        }

        private static void minimalResistances(final ListMultimap<IEnergyConsumer, GeneratorRoute> routes) {
            for (IEnergyConsumer key : routes.keys()) {
                var values = routes.get(key);
                values.sort(GeneratorRoute.COMPARATOR);

                GeneratorRoute prev = null;
                var iter = values.iterator();
                while (iter.hasNext()) {
                    var curr = iter.next();
                    if (prev != null && prev.generator == curr.generator) {
                        iter.remove();
                    } else {
                        prev = curr;
                    }
                }
            }
        }

        private static GeneratorRoute buildRoute(IEnergyConsumer consumer, HashMap<IEnergyComponent, IEnergyComponent> parentMap) {
            ArrayList<IWire> path = new ArrayList<>();
            long totalResistance = 0;
            IEnergyComponent curr = consumer;
            while (true) {
                var father = Objects.requireNonNull(parentMap.get(curr));
                if (father == Flag.ROOT) {
                    // definitely safe
                    return new GeneratorRoute((IEnergyGenerator) curr, totalResistance, path.reversed());
                }
                // safe
                IWire w = (IWire) father;
                path.add(w);
                totalResistance += w.getResistance();
                curr = father;
            }
        }

        // todo: Incremental Update
        public void updateFully(Graph<IEnergyComponent> graph) {
            var nodes = graph.nodes();
            adjacentComponent = HashMultimap.create(nodes.size(), 2);


            updateComponents(graph);
            for (IEnergyComponent node : nodes) {
                for (Direction d : DIRECTIONS) {
                    long pos = node.getPos().relative(d).asLong();
                    if (pos2Component.containsKey(pos)) {
                        adjacentComponent.put(node, pos2Component.get(pos));
                    }
                }
            }
            updateNetwork(graph, generators);
            minimalResistances(energyNetwork);
        }

        /**
         * Update the cache, except {@link Cache#adjacentComponent}
         */
        public void updateComponents(Graph<IEnergyComponent> graph) {
            var nodes = graph.nodes();
            pos2Component = new Long2ReferenceOpenHashMap<>(nodes.size());
            generators = new HashSet<>();
            consumers = new HashSet<>();
            wires = new HashSet<>();

            for (IEnergyComponent node : nodes) {
                if (node instanceof IEnergyGenerator g) generators.add(g);
                if (node instanceof IEnergyConsumer c) consumers.add(c);
                if (node instanceof IWire w) wires.add(w);
                pos2Component.put(node.getPos().asLong(), node);
            }

            generatorView = Collections.unmodifiableSet(generators);
            consumerView = Collections.unmodifiableSet(consumers);
            wireView = Collections.unmodifiableSet(wires);
            pos2ComponentView = Long2ReferenceMaps.unmodifiable(pos2Component);
        }

        public @UnmodifiableView Set<IEnergyGenerator> getGenerators() {
            return generatorView;
        }

        public @UnmodifiableView Set<IEnergyConsumer> getConsumers() {
            return consumerView;
        }

        public @UnmodifiableView Set<IWire> getWires() {
            return wireView;
        }

        public @UnmodifiableView ListMultimap<IEnergyConsumer, GeneratorRoute> getEnergyNetwork() {
            return energyNetworkView;
        }

        public @UnmodifiableView Multimap<IEnergyComponent, IEnergyComponent> getAdjacentComponent() {
            return adjacentComponentView;
        }

        public @UnmodifiableView Long2ReferenceMap<IEnergyComponent> getPos2Component() {
            return pos2ComponentView;
        }

        private void rebuildAllViews() {
            generatorView = Collections.unmodifiableSet(generators);
            consumerView = Collections.unmodifiableSet(consumers);
            wireView = Collections.unmodifiableSet(wires);
            energyNetworkView = Multimaps.unmodifiableListMultimap(energyNetwork);
            adjacentComponentView = Multimaps.unmodifiableMultimap(adjacentComponent);
            pos2ComponentView = Long2ReferenceMaps.unmodifiable(pos2Component);
        }

        private void updateNetwork(Graph<IEnergyComponent> graph, Collection<IEnergyGenerator> generators) {
            energyNetwork = MultimapBuilder.hashKeys(generators.size()).arrayListValues().build();

            HashMap<IEnergyComponent, IEnergyComponent> parentMap = HashMap.newHashMap(graph.nodes().size());
            Set<IEnergyComponent> visited = HashSet.newHashSet(graph.nodes().size());
            ArrayDeque<IEnergyComponent> visiting = new ArrayDeque<>();

            for (IEnergyGenerator generator : generators) {
                parentMap.clear();
                visited.clear();
                visiting.clear();

                parentMap.put(generator, Flag.ROOT);
                for (IEnergyComponent successor : graph.successors(generator)) {
                    if (visited.add(successor)) {
                        parentMap.put(successor, generator);
                        visiting.addLast(successor);
                    }
                }
                // bfs
                while (!visiting.isEmpty()) {
                    var curr = visiting.removeFirst();
                    switch (curr) {
                        case IWire wire -> {
                            for (IEnergyComponent successor : graph.successors(wire)) {
                                if (visited.add(successor)) {
                                    parentMap.put(successor, curr);
                                    visiting.addLast(successor);
                                }
                            }
                        }
                        case IEnergyConsumer con -> energyNetwork.put(con, buildRoute(con, parentMap));
                        default -> {
                        }
                    }
                }
            }
            energyNetworkView = Multimaps.unmodifiableListMultimap(energyNetwork);
        }
    }
}
