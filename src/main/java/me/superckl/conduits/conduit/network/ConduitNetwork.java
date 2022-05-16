package me.superckl.conduits.conduit.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.RequiredArgsConstructor;
import me.superckl.conduits.common.block.ConduitBlockEntity;
import me.superckl.conduits.conduit.ConduitType;
import me.superckl.conduits.conduit.connection.ConduitConnectionType;
import me.superckl.conduits.util.GraphUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class ConduitNetwork {

	private final Level level;
	private final ConduitType type;
	private final ConduitNetworkGraph graph;

	private final List<ConduitBlockEntity> changedBEs = new ArrayList<>();

	public ConduitNetwork(final Level level, final ConduitType type) {
		this(level, type, new ConduitNetworkGraph(type));
	}

	private ConduitNetwork(final Level level, final ConduitType type, final ConduitNetworkGraph graph) {

		this.level = level;
		this.type = type;
		this.graph = graph;
		if(!level.isClientSide)
			this.level.getCapability(NetworkTicker.CAPABILITY).ifPresent(ticker -> ticker.add(this));
	}

	/**
	 * Merges the passed conduit into this network. This will establish new edges but
	 * does not do any checking for previously disjoint networks. See {@link ConduitNetwork#mergeOrEstablish}.
	 * WARNING: No validation is done on the state of this network after the merge, though it should be valid
	 * @return This network for method chaining
	 */
	private ConduitNetwork accept(final ConduitBlockEntity conduit, final boolean connectionChange) {
		final BlockPos pos = conduit.getBlockPos();
		this.graph.addNode(pos, conduit);
		conduit.setNetwork(this.type, this);
		if(connectionChange)
			this.changedBEs.add(conduit);
		return this;
	}

	/**
	 * Merges the passed network into this network.
	 * WARNING: No validation is done on the state of this network after the merge.
	 * @return This network for method chaining
	 */
	private ConduitNetwork merge(final ConduitNetwork other) {
		if(this == other)
			return this;
		if(this.type != other.type)
			throw new UnsupportedOperationException(String.format("Cannot merge networks with differing types! %s, %s", this.type, other.type));
		//Claim the other network's conduits
		other.graph.conduits().forEach(x -> this.accept(x, false));
		//Copy over it's connection graph for the claimed conduits
		this.graph.mergeConnections(other.graph);
		//Copy over any changes that the other network had pending
		this.changedBEs.addAll(other.changedBEs);
		//invalidate the other network
		other.invalidate();
		return this;
	}

	/**
	 * This will clear any references to things like block entities.
	 * This purposefully leaves the network in an invalid state so that it errors
	 * if used afterwards.
	 */
	private void invalidate() {
		this.changedBEs.clear();
		this.graph.invalidate();
		this.level.getCapability(NetworkTicker.CAPABILITY).ifPresent(ticker -> ticker.remove(this));
	}

	/**
	 * Helper method to merge the smaller network into the bigger network for efficiency
	 * @return the merged network that should be used for subsequent method calls. This will
	 * be one of the passed networks.
	 */
	private static ConduitNetwork mergeSmaller(final ConduitNetwork network1, final ConduitNetwork network2) {
		if(network1 == network2)
			return network1;
		if(network1.graph.size() > network2.graph.size())
			return network1.merge(network2);
		return network2.merge(network1);
	}

	@SuppressWarnings("resource")
	public void connectionChange(final ConduitBlockEntity conduit) {
		if(conduit.getLevel().isClientSide)
			return;
		//Connection changes are unique because they can occur before
		//the neighboring conduit has determined its state and declared itself to the network
		//The network changing must be delayed until it next ticks
		this.changedBEs.add(conduit);
	}

	@SuppressWarnings("resource")
	public void removed(final ConduitBlockEntity conduit) {
		if(conduit.getLevel().isClientSide)
			return;
		this.graph.removeNode(conduit.getBlockPos());
		if(this.graph.isEmpty())
			this.invalidate();
		else
			this.splitDisconnected();
	}

	private ConduitNetwork mergeConnected(final ConduitBlockEntity conduit) {
		final Collection<ConduitBlockEntity> connected = conduit.getConnectedConduits(this.type);
		final List<ConduitNetwork> others = connected.stream().map(con -> ConduitNetwork.getOrEstablish(this.type, con))
				.distinct().collect(Collectors.toCollection(ArrayList::new));
		if(!others.contains(this))
			others.add(this);
		return others.stream().reduce(ConduitNetwork::mergeSmaller).orElse(this);
	}

	private List<ConduitNetwork> splitDisconnected() {
		if(this.graph.isConnected())
			return ImmutableList.of(this);
		final List<ConduitNetwork> split = this.graph.splitDisconnected().stream().map(graph -> {
			final ConduitNetwork network = new ConduitNetwork(this.level, this.type, graph);
			//Set the network for all the conduits split off into the new network
			network.graph.conduits().forEach(x -> x.setNetwork(this.type, network));
			return network;
		}).collect(Collectors.toList());
		this.invalidate();
		return split;
	}

	private static ConduitNetwork getOrEstablish(final ConduitType type, final ConduitBlockEntity conduit) {
		return conduit.getNetwork(type).orElseGet(() -> ConduitNetwork.establish(conduit, type));
	}

	@SuppressWarnings("resource")
	public static void mergeOrEstablish(final ConduitBlockEntity conduit) {
		if(conduit.getLevel().isClientSide)
			return;
		conduit.getConnections().types().forEach(type ->{
			ConduitNetwork.getOrEstablish(type, conduit).mergeConnected(conduit);
		});
	}

	@SuppressWarnings("resource")
	public static ConduitNetwork establish(final ConduitBlockEntity conduit, final ConduitType type) {
		if(conduit.getLevel().isClientSide)
			throw new IllegalStateException("Cannot create networks on the client side!");
		return new ConduitNetwork(conduit.getLevel(), type).accept(conduit, true);
	}

	public void rescan() {
		if(this.changedBEs.isEmpty())
			return;
		//NOTE: This is quite a delicate operation with many joins/splits occuring in a given rescan
		//It's very easy to mistakingly use an invalidated network or to loose some connectivity data

		//Merging a network with pending changes may add the pending changes to this network.
		//Thus, we iterate until all changes have been taken care of.
		while(!this.changedBEs.isEmpty()) {
			//Scanning may cause the network to modify the changed BEs list, so we copy it and then remove
			//what was already there
			final List<ConduitBlockEntity> copy = new ArrayList<>(this.changedBEs);
			//We do not use THIS network here, the merging and splitting that can happen
			//might invalidate this network. Instead we get the network the conduit belongs to
			copy.stream().distinct().filter(Predicates.not(ConduitBlockEntity::isRemoved))
			.filter(conduit -> ConduitNetwork.getOrEstablish(this.type, conduit).graph.hasNode(conduit.getBlockPos())).forEach(conduit -> {
				final ConduitNetwork network = ConduitNetwork.getOrEstablish(this.type, conduit);
				//Let the graph make the proper changes
				network.graph.putConnections(conduit);
				//If this network was split off from this one, make sure it doesn't have to recheck this conduit later
				network.changedBEs.remove(conduit);
				//Check for newly merged or disconnected networks
				network.mergeConnected(conduit).splitDisconnected();
			});
			this.changedBEs.removeAll(copy);
		}
	}

	public void tick() {
		if(this.graph.isInvalid())
			return;
		this.rescan();
	}

	@Override
	public String toString() {
		return new StringBuilder("ConduitNetwork[Type: ").append(this.type.getSerializedName())
				.append(", Graph: ").append(this.graph).append("]").toString();
	}

	/**
	 * Helper class that enforces data validity. This includes:
	 * 1. The connection graph and conduit map always contain the same block positions
	 */
	@RequiredArgsConstructor
	private static class ConduitNetworkGraph{

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

}