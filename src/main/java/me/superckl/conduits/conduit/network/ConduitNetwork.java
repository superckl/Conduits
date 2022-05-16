package me.superckl.conduits.conduit.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.base.Predicates;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.superckl.conduits.common.block.ConduitBlockEntity;
import me.superckl.conduits.conduit.ConduitType;
import me.superckl.conduits.conduit.connection.ConduitConnectionType;
import me.superckl.conduits.util.GraphUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class ConduitNetwork {

	private final Level level;
	private final ConduitType type;
	private final Map<BlockPos, ConduitBlockEntity> conduits;
	private final MutableGraph<BlockPos> connectionGraph;

	private final List<ConduitBlockEntity> changedBEs = new ArrayList<>();

	private boolean invalid = false;

	public ConduitNetwork(final Level level, final ConduitType type) {
		this(level, type, new Object2ObjectOpenHashMap<>(), GraphBuilder.undirected().allowsSelfLoops(false).build());
	}

	private ConduitNetwork(final Level level, final ConduitType type, final Map<BlockPos, ConduitBlockEntity> conduits,
			final MutableGraph<BlockPos> connectionGraph) {
		this.level = level;
		this.type = type;
		this.conduits = conduits;
		this.connectionGraph = connectionGraph;
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
		this.conduits.put(pos, conduit);
		this.connectionGraph.removeNode(pos);
		this.connectionGraph.addNode(pos);
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
		other.conduits.values().forEach(x -> this.accept(x, false));
		//Copy over it's connection graph for the claimed conduits
		GraphUtil.merge(this.connectionGraph, other.connectionGraph);
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
		this.invalid = true;
		this.changedBEs.clear();
		this.conduits.clear();
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
		if(network1.conduits.size() > network2.conduits.size())
			return network1.merge(network2);
		return network2.merge(network1);
	}

	private ConduitNetwork putConnections(final ConduitBlockEntity conduit) {
		final BlockPos pos = conduit.getBlockPos();
		this.connectionGraph.addNode(pos);
		conduit.getConnections().getConnections(this.type).forEach((dir, connType) -> {
			if(connType == ConduitConnectionType.CONDUIT)
				this.connectionGraph.putEdge(pos, pos.relative(dir));
		});
		return this;
	}

	@SuppressWarnings("resource")
	public void connectionChange(final ConduitBlockEntity conduit) {
		if(conduit.getLevel().isClientSide)
			return;
		//Connection changes are unique because they can occur before
		//the neighboring conduit has determined its state and declared itself to the network
		//The network changing must be delayed until it next ticks
		this.changedBEs.add(conduit);
		//this.putConnections(conduit);
		//this.mergeConnected(conduit).splitDisconnected();
	}

	@SuppressWarnings("resource")
	public void removed(final ConduitBlockEntity conduit) {
		if(conduit.getLevel().isClientSide)
			return;
		this.conduits.remove(conduit.getBlockPos());
		this.connectionGraph.removeNode(conduit.getBlockPos());
		if(this.conduits.isEmpty())
			this.invalidate();
		else
			this.splitDisconnected();
	}

	private ConduitNetwork mergeConnected(final ConduitBlockEntity conduit) {
		final Collection<ConduitBlockEntity> connected = conduit.getConnectedConduits(this.type);
		final ConduitNetwork merged = connected.stream().map(con -> ConduitNetwork.getOrEstablish(this.type, con))
				.reduce(ConduitNetwork::mergeSmaller).orElse(this);
		return ConduitNetwork.mergeSmaller(this, merged);
	}

	private List<ConduitNetwork> splitDisconnected() {
		final List<Set<BlockPos>> fills = GraphUtil.floodFill(this.connectionGraph);
		return fills.stream().map(nodes -> {
			final Map<BlockPos, ConduitBlockEntity> conduits = nodes.stream().collect(Collectors.toMap(Function.identity(), this.conduits::get));
			final MutableGraph<BlockPos> graph = GraphBuilder.undirected().allowsSelfLoops(false).build();
			nodes.forEach(pos -> {
				graph.addNode(pos);
				this.connectionGraph.incidentEdges(pos).forEach(graph::putEdge);
			});
			final ConduitNetwork network = new ConduitNetwork(this.level, this.type, conduits, graph);
			conduits.values().forEach(x -> x.setNetwork(this.type, network));
			network.verifyState();
			return network;
		}).collect(Collectors.toList());
	}

	private void verifyState() {
		if(!this.conduits.keySet().equals(this.connectionGraph.nodes()))
			throw new IllegalStateException("Conduit map keys does not match graph nodes!");
		if(!GraphUtil.isConnected(this.connectionGraph))
			throw new IllegalStateException("Network graph is disconnected!");
		if(!this.conduits.values().stream().allMatch(conduit -> conduit.getNetwork(this.type).orElseThrow() == this))
			throw new IllegalStateException("Conduit-Network mapping mismatch!");
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
		//Merging a network with pending changes will cause add the pending changes to this network.
		//Thus, we iterate until all changes have been taken care of.
		while(!this.changedBEs.isEmpty()) {
			//Scanning may cause the network to modify the changed BEs list, so we copy it and then remove
			//what was already there
			final List<ConduitBlockEntity> copy = new ArrayList<>(this.changedBEs);
			copy.stream().filter(Predicates.not(ConduitBlockEntity::isRemoved))
			.filter(conduit -> this.conduits.containsKey(conduit.getBlockPos())).forEach(conduit -> {
				ConduitNetwork.getOrEstablish(this.type, conduit).putConnections(conduit).mergeConnected(conduit).splitDisconnected();
			});
			this.changedBEs.removeAll(copy);
		}
	}

	public void tick() {
		if(this.invalid)
			return;
		this.rescan();
	}

	@Override
	public String toString() {
		return new StringBuilder("ConduitNetwork[Type: ").append(this.type.getSerializedName())
				.append(", Conduit Positions: ").append(this.conduits.keySet())
				.append(", Graph Nodes: ").append(this.connectionGraph.nodes())
				.append(", Graph Edges: ").append(this.connectionGraph.edges()).append("]").toString();
	}

}
