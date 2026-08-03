package com.mistbeyond.examplemod.core.logistic.impl;

import com.google.common.collect.*;
import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.mistbeyond.examplemod.core.VoltageTier;
import com.mistbeyond.examplemod.core.logistic.energy.*;
import com.mistbeyond.examplemod.util.Util;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMaps;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

@Slf4j
public class EnergyNetwork implements IEnergyNetwork {
    private final ServerLevel level;
    private final MutableGraph<IEnergyComponent> componentGraph = GraphBuilder.undirected().build();
    private final Cache cache = Cache.newEmpty();

    public EnergyNetwork(ServerLevel level) {
        this.level = level;
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
    public EUTransferInfo pullEnergy(IEnergyConsumer consumer, EUTransferInfo info, TransactionContext transaction) {
        long requested = info.power();
        if (requested <= 0) {
            return EUTransferInfo.ZERO;
        }
        List<GeneratorRoute> routes = cache.energyNetworkView.get(consumer);
        if (routes.isEmpty()) {
            return EUTransferInfo.ZERO;
        }
        try (Transaction actualTransaction = Transaction.open(transaction)) {
            for (GeneratorRoute route : routes) {
                IEnergyGenerator generator = route.generator();
                long voltage = generator.getGeneratorVoltageTier().value;
                if (voltage <= 0) {
                    continue;
                }
                long loss = lineLoss(route.totalResistance(), requested / voltage);
                long supply = saturatedAdd(requested, loss);
                long delivered;
                EUTransferInfo accepted;
                try (Transaction routeTransaction = Transaction.open(actualTransaction)) {
                    if (generator.extractEnergy(supply, routeTransaction).power() != supply) {
                        // This generator cannot cover the whole request; fall through to the next route.
                        continue;
                    }
                    delivered = supply - loss;
                    accepted = consumer.insertEU(
                            EUTransferInfo.power(generator.getGeneratorVoltageTier(), delivered),
                            routeTransaction
                    );
                    if (accepted.power() != delivered) {
                        // The consumer did not accept the whole delivery; try the next route.
                        continue;
                    }
                    routeTransaction.commit();
                }
                EUTransferInfo wireLoad = EUTransferInfo.power(generator.getGeneratorVoltageTier(), supply);
                for (IWire wire : route.path()) {
                    wire.applyElectricLoad(wireLoad);
                }
                actualTransaction.commit();
                return accepted;
            }
        }
        return EUTransferInfo.ZERO;
    }

    /**
     * Line loss on a route for one tick: {@code resistance * current^2}, saturated.
     */
    private static long lineLoss(long totalResistance, long current) {
        return Util.saturatedPositiveMultiply(totalResistance, Util.saturatedPositiveMultiply(current, current));
    }

    private static long saturatedAdd(long a, long b) {
        long sum = a + b;
        return sum < 0 ? Long.MAX_VALUE : sum;
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
        if (!componentGraph.removeNode(component)) {
            return;
        }
        List<Set<IEnergyComponent>> parts = connectedComponents();
        if (parts.size() > 1) {
            Set<IEnergyComponent> kept = parts.getFirst();
            for (IEnergyComponent node : Set.copyOf(componentGraph.nodes())) {
                if (!kept.contains(node)) {
                    componentGraph.removeNode(node);
                }
            }
            for (int i = 1; i < parts.size(); i++) {
                EnergyNetwork split = new EnergyNetwork(level);
                parts.get(i).forEach(c -> split.addComponent(c, true));
                split.onComponentModified();
                EnergyNetworkManager.INSTANCE.register(split);
            }
        }
        onComponentModified();
        if (componentGraph.nodes().isEmpty()) {
            EnergyNetworkManager.INSTANCE.unregister(this);
        }
    }

    /**
     * Splits the remaining graph nodes into connected components (BFS).
     */
    private List<Set<IEnergyComponent>> connectedComponents() {
        Set<IEnergyComponent> remaining = new HashSet<>(componentGraph.nodes());
        List<Set<IEnergyComponent>> parts = new ArrayList<>();
        while (!remaining.isEmpty()) {
            IEnergyComponent start = remaining.iterator().next();
            Set<IEnergyComponent> part = new HashSet<>();
            ArrayDeque<IEnergyComponent> visiting = new ArrayDeque<>();
            remaining.remove(start);
            part.add(start);
            visiting.addLast(start);
            while (!visiting.isEmpty()) {
                IEnergyComponent curr = visiting.removeFirst();
                for (IEnergyComponent adjacent : componentGraph.adjacentNodes(curr)) {
                    if (remaining.remove(adjacent)) {
                        part.add(adjacent);
                        visiting.addLast(adjacent);
                    }
                }
            }
            parts.add(part);
        }
        return parts;
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

        for (IEnergyComponent other : componentGraph.nodes()) {
            if (other != component && other.isConnectWith(component)) {
                componentGraph.putEdge(component, other);
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
        public EUTransferInfo insertEU(EUTransferInfo info, TransactionContext transaction) {
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
        private @UnmodifiableView ListMultimap<IEnergyConsumer, GeneratorRoute> energyNetworkView;
        private @UnmodifiableView Long2ReferenceMap<IEnergyComponent> pos2ComponentView;
        private HashSet<IEnergyGenerator> generators;
        private HashSet<IEnergyConsumer> consumers;
        private ListMultimap<IEnergyConsumer, GeneratorRoute> energyNetwork;
        private Long2ReferenceMap<IEnergyComponent> pos2Component;

        private Cache(HashSet<IEnergyGenerator> generators, HashSet<IEnergyConsumer> consumers, ListMultimap<IEnergyConsumer, GeneratorRoute> energyNetwork, Long2ReferenceOpenHashMap<IEnergyComponent> pos2Component) {
            this.generators = generators;
            this.consumers = consumers;
            this.energyNetwork = energyNetwork;
            this.pos2Component = pos2Component;
            rebuildAllViews();
        }

        public static Cache newEmpty() {
            return new Cache(new HashSet<>(), new HashSet<>(), MultimapBuilder.hashKeys().arrayListValues().build(), new Long2ReferenceOpenHashMap<>());
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
                // Only wires are intermediate nodes on a route; the terminal generator is not a wire.
                if (father instanceof IWire wire) {
                    path.add(wire);
                    totalResistance += wire.getResistance();
                }
                curr = father;
            }
        }

        // todo: Incremental Update
        public void updateFully(Graph<IEnergyComponent> graph) {
            updateComponents(graph);
            updateNetwork(graph, generators);
            minimalResistances(energyNetwork);
        }

        /**
         * Updates the component collections and position index.
         */
        public void updateComponents(Graph<IEnergyComponent> graph) {
            var nodes = graph.nodes();
            pos2Component = new Long2ReferenceOpenHashMap<>(nodes.size());
            generators = new HashSet<>();
            consumers = new HashSet<>();

            for (IEnergyComponent node : nodes) {
                if (node instanceof IEnergyGenerator g) generators.add(g);
                if (node instanceof IEnergyConsumer c) consumers.add(c);
                pos2Component.put(node.getPos().asLong(), node);
            }

            generatorView = Collections.unmodifiableSet(generators);
            consumerView = Collections.unmodifiableSet(consumers);
            pos2ComponentView = Long2ReferenceMaps.unmodifiable(pos2Component);
        }

        private void rebuildAllViews() {
            generatorView = Collections.unmodifiableSet(generators);
            consumerView = Collections.unmodifiableSet(consumers);
            energyNetworkView = Multimaps.unmodifiableListMultimap(energyNetwork);
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
