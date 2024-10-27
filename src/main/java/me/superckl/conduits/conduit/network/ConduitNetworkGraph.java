package me.superckl.conduits.conduit.network;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.RequiredArgsConstructor;
import me.superckl.conduits.common.block.ConduitBlockEntity;
import me.superckl.conduits.conduit.ConduitType;
import me.superckl.conduits.conduit.connection.ConduitConnection.Inventory;
import me.superckl.conduits.conduit.network.inventory.TransferrableQuantity;
import me.superckl.conduits.util.GraphUtil;
import me.superckl.conduits.util.Positioned;
import me.superckl.conduits.util.PositionedCache;
import net.minecraft.core.BlockPos;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Helper class that enforces data validity. This includes:
 * 1. The connection graph and conduit map always contain the same block positions
 * 2. Maintaining a list of conduits that have inventory connections
 * 3. Throwing an exception if the graph is used after being invalidated
 */
@RequiredArgsConstructor
public class ConduitNetworkGraph<T extends TransferrableQuantity> {

    private final ConduitType<T> type;
    private final Map<BlockPos, ConduitBlockEntity> conduits;
    private final PositionedCache<Inventory<T>> inventories;
    private final MutableGraph<BlockPos> connectionGraph;
    private boolean invalid = false;

    public ConduitNetworkGraph(final ConduitType<T> type) {
        this(type, new Object2ObjectOpenHashMap<>(), new PositionedCache<>(),
                GraphBuilder.undirected().allowsSelfLoops(false).build());
    }

    public boolean removeNode(final BlockPos pos) {
        this.checkInvalid();
        this.conduits.remove(pos);
        this.inventories.removeAll(pos);
        return this.connectionGraph.removeNode(pos);
    }

    public boolean addNode(final BlockPos pos, final ConduitBlockEntity conduit) {
        this.checkInvalid();
        this.conduits.put(pos, conduit);
        return this.connectionGraph.addNode(pos);
    }

    public boolean hasNode(final BlockPos pos) {
        this.checkInvalid();
        return this.conduits.containsKey(pos);
    }

    public Collection<ConduitBlockEntity> conduits() {
        this.checkInvalid();
        return Collections.unmodifiableCollection(this.conduits.values());
    }

    public Pair<List<Positioned<Inventory<T>>>, List<Positioned<Inventory<T>>>> gatherInventories(final ConduitType<T> type) {
        final List<Positioned<Inventory<T>>> accepting = new ArrayList<>();
        final List<Positioned<Inventory<T>>> providing = new ArrayList<>();
        this.inventories.stream().forEach(pos -> {
            if (pos.value().getSettings().isAccepting())
                accepting.add(pos);
            if (pos.value().getSettings().isProviding())
                providing.add(pos);
        });
        return Pair.of(accepting, providing);
    }

    public void mergeConnections(final ConduitNetworkGraph<T> other) {
        this.checkInvalid();
        final Set<BlockPos> nodes = this.connectionGraph.nodes();
        if (!nodes.containsAll(other.connectionGraph.nodes()))
            throw new IllegalArgumentException("Cannot merge graph that contains nodes this graph does not contain!");
        GraphUtil.merge(this.connectionGraph, other.connectionGraph);
        this.inventories.addAll(other.inventories);
    }

    public void putConnections(final ConduitBlockEntity conduit) {
        this.checkInvalid();
        final BlockPos pos = conduit.getBlockPos();
        if (!this.conduits.containsKey(pos) || this.conduits.get(pos) != conduit)
            throw new IllegalArgumentException("Conduit is not a member of this network!");
        //Clear the existing edges
        this.connectionGraph.removeNode(pos);
        this.inventories.removeAll(pos);
        this.connectionGraph.addNode(pos);
        conduit.getConnections().getConnections(this.type).forEach((dir, conn) -> {
            switch (conn.getConnectionType()) {
                case CONDUIT -> this.connectionGraph.putEdge(pos, pos.relative(dir));
                case INVENTORY ->
                        this.inventories.add(new Positioned<>(pos, conn.asInventory().restoreGeneric(this.type)));
                default -> throw new IllegalStateException("Unsupported connection type " + conn.getType());
            }
        });
    }

    public boolean isConnected() {
        this.checkInvalid();
        return GraphUtil.isConnected(this.connectionGraph);
    }

    public List<ConduitNetworkGraph<T>> splitDisconnected() {
        this.checkInvalid();
        final List<Set<BlockPos>> fills = GraphUtil.floodFill(this.connectionGraph);
        return fills.stream().map(nodes -> {
            final Map<BlockPos, ConduitBlockEntity> conduits = nodes.stream()
                    .collect(Collectors.toMap(Function.identity(), this.conduits::get,
                            (x, y) -> {
                                throw new UnsupportedOperationException();
                            }, Object2ObjectOpenHashMap::new));
            final PositionedCache<Inventory<T>> inventories = this.inventories.filter(nodes::contains);
            final MutableGraph<BlockPos> graph = GraphBuilder.undirected().allowsSelfLoops(false).build();
            nodes.forEach(pos -> {
                graph.addNode(pos);
                this.connectionGraph.incidentEdges(pos).forEach(graph::putEdge);
            });
            return new ConduitNetworkGraph<>(this.type, conduits, inventories, graph);
        }).collect(Collectors.toList());
    }

    public Object2IntMap<BlockPos> computeDistanceMap(final BlockPos origin) {
        if (!this.conduits.containsKey(origin))
            throw new IllegalArgumentException("Cannot compute distance from node not in graph!");
        return GraphUtil.distances(this.connectionGraph, origin);
    }

    public int size() {
        this.checkInvalid();
        return this.conduits.size();
    }

    public boolean isEmpty() {
        this.checkInvalid();
        return this.conduits.isEmpty();
    }

    public void invalidate() {
        this.invalid = true;
    }

    public boolean isInvalid() {
        return this.invalid;
    }

    public void checkInvalid() {
        if (this.invalid)
            throw new IllegalStateException("Attempted to use an invalidated network graph!");
    }

    @Override
    public String toString() {
        this.checkInvalid();
        final StringBuilder builder = new StringBuilder("ConduitNetworkGraph[Type: ").append(this.type.getResourceLocation().getPath())
                .append(", Conduit Positions: [");
        this.conduits.keySet().forEach(pos -> {
            builder.append(pos);
            if (this.inventories.contains(pos))
                builder.append('(').append(this.conduits.get(pos).getConnectedInventories(this.type).size()).append(')');
            builder.append(',');
        });
        builder.deleteCharAt(builder.length() - 1);
        builder.append(']');
        return builder.append(", Graph Nodes: ").append(this.connectionGraph.nodes())
                .append(", Graph Edges: ").append(this.connectionGraph.edges()).append("]").toString();
    }

}