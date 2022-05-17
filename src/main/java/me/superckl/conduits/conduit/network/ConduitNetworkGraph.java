package me.superckl.conduits.conduit.network;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.RequiredArgsConstructor;
import me.superckl.conduits.common.block.ConduitBlockEntity;
import me.superckl.conduits.conduit.ConduitType;
import me.superckl.conduits.conduit.connection.ConduitConnectionType;
import me.superckl.conduits.util.GraphUtil;
import net.minecraft.core.BlockPos;

/**
 * Helper class that enforces data validity. This includes:
 * 1. The connection graph and conduit map always contain the same block positions
 */
@RequiredArgsConstructor
public class ConduitNetworkGraph{

	private final ConduitType type;
	private final Map<BlockPos, ConduitBlockEntity> conduits;
	private final MutableGraph<BlockPos> connectionGraph;
	private boolean invalid = false;

	public ConduitNetworkGraph(final ConduitType type) {
		this(type, new Object2ObjectOpenHashMap<>(), GraphBuilder.undirected().allowsSelfLoops(false).build());
	}

	public boolean removeNode(final BlockPos pos) {
		this.checkInvalid();
		this.conduits.remove(pos);
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

	public Collection<ConduitBlockEntity> conduits(){
		this.checkInvalid();
		return Collections.unmodifiableCollection(this.conduits.values());
	}

	public void mergeConnections(final ConduitNetworkGraph other) {
		this.checkInvalid();
		final Set<BlockPos> nodes = this.connectionGraph.nodes();
		if(!nodes.containsAll(other.connectionGraph.nodes()))
			throw new IllegalArgumentException("Cannot merge graph that contains nodes this graph does not contain!");
		GraphUtil.merge(this.connectionGraph, other.connectionGraph);
	}

	public void putConnections(final ConduitBlockEntity conduit) {
		this.checkInvalid();
		final BlockPos pos = conduit.getBlockPos();
		if(!this.conduits.containsKey(pos) || this.conduits.get(pos) != conduit)
			throw new IllegalArgumentException("Conduit is not a member of this network!");
		//Clear the existing edges
		this.connectionGraph.removeNode(pos);
		this.connectionGraph.addNode(pos);
		conduit.getConnections().getConnections(this.type).forEach((dir, connType) -> {
			if(connType == ConduitConnectionType.CONDUIT)
				this.connectionGraph.putEdge(pos, pos.relative(dir));
		});
	}

	public boolean isConnected() {
		this.checkInvalid();
		return GraphUtil.isConnected(this.connectionGraph);
	}

	public List<ConduitNetworkGraph> splitDisconnected() {
		this.checkInvalid();
		final List<Set<BlockPos>> fills = GraphUtil.floodFill(this.connectionGraph);
		return fills.stream().map(nodes -> {
			final Map<BlockPos, ConduitBlockEntity> conduits = nodes.stream()
					.collect(Collectors.toMap(Function.identity(), this.conduits::get,
							(x, y) -> {throw new UnsupportedOperationException();}, Object2ObjectOpenHashMap::new));
			final MutableGraph<BlockPos> graph = GraphBuilder.undirected().allowsSelfLoops(false).build();
			nodes.forEach(pos -> {
				graph.addNode(pos);
				this.connectionGraph.incidentEdges(pos).forEach(graph::putEdge);
			});
			return new ConduitNetworkGraph(this.type, conduits, graph);
		}).collect(Collectors.toList());
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
		if(this.invalid)
			throw new IllegalStateException("Attempted to use an invalidated network graph!");
	}

	@Override
	public String toString() {
		this.checkInvalid();
		return new StringBuilder("ConduitNetworkGraph[Type: ").append(this.type.getSerializedName())
				.append(", Conduit Positions: ").append(this.conduits.keySet())
				.append(", Graph Nodes: ").append(this.connectionGraph.nodes())
				.append(", Graph Edges: ").append(this.connectionGraph.edges()).append("]").toString();
	}

}