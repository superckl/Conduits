package me.superckl.conduits.conduit.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import me.superckl.conduits.common.block.ConduitBlockEntity;
import me.superckl.conduits.conduit.ConduitType;
import me.superckl.conduits.conduit.connection.ConduitConnection.Inventory;
import me.superckl.conduits.conduit.network.inventory.TransferrableQuantity;
import me.superckl.conduits.util.Positioned;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class ConduitNetwork<T extends TransferrableQuantity> {

	private final Level level;
	private final ConduitType<T> type;
	private final ConduitNetworkGraph<T> graph;

	private final List<ConduitBlockEntity> changedBEs = new ArrayList<>();

	public ConduitNetwork(final Level level, final ConduitType<T> type) {
		this(level, type, new ConduitNetworkGraph<>(type));
	}

	private ConduitNetwork(final Level level, final ConduitType<T> type, final ConduitNetworkGraph<T> graph) {

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
	private ConduitNetwork<T> accept(final ConduitBlockEntity conduit, final boolean connectionChange) {
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
	private ConduitNetwork<T> merge(final ConduitNetwork<T> other) {
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
	private static <T extends TransferrableQuantity> ConduitNetwork<T> mergeSmaller(final ConduitNetwork<T> network1, final ConduitNetwork<T> network2) {
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
		this.changedBEs.remove(conduit);
		if(this.graph.isEmpty())
			this.invalidate();
		else
			this.splitDisconnected();
	}

	private ConduitNetwork<T> mergeConnected(final ConduitBlockEntity conduit) {
		final Collection<ConduitBlockEntity> connected = conduit.getConnectedConduits(this.type);
		final List<ConduitNetwork<T>> others = connected.stream().map(con -> ConduitNetwork.getOrEstablish(this.type, con))
				.distinct().collect(Collectors.toCollection(ArrayList::new));
		if(!others.contains(this))
			others.add(this);
		return others.stream().reduce(ConduitNetwork::mergeSmaller).orElse(this);
	}

	private List<ConduitNetwork<T>> splitDisconnected() {
		if(this.graph.isConnected())
			return ImmutableList.of(this);
		final List<ConduitNetwork<T>> split = this.graph.splitDisconnected().stream().map(graph -> {
			final ConduitNetwork<T> network = new ConduitNetwork<>(this.level, this.type, graph);
			//Set the network for all the conduits split off into the new network
			network.graph.conduits().forEach(x -> x.setNetwork(this.type, network));
			return network;
		}).collect(Collectors.toList());
		this.invalidate();
		return split;
	}

	public Object2IntMap<BlockPos> computeDistanceMap(final BlockPos origin){
		return this.graph.computeDistanceMap(origin);
	}

	private static <T extends TransferrableQuantity> ConduitNetwork<T> getOrEstablish(final ConduitType<T> type, final ConduitBlockEntity conduit) {
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
	public static <T extends TransferrableQuantity> ConduitNetwork<T> establish(final ConduitBlockEntity conduit, final ConduitType<T> type) {
		if(conduit.getLevel().isClientSide)
			throw new IllegalStateException("Cannot create networks on the client side!");
		return new ConduitNetwork<>(conduit.getLevel(), type).accept(conduit, true);
	}

	private void rescan() {
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
				final ConduitNetwork<T> network = ConduitNetwork.getOrEstablish(this.type, conduit);
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
		final Pair<List<Positioned<Inventory<T>>>, List<Positioned<Inventory<T>>>> inventories = this.graph.gatherInventories(this.type);
		final List<Positioned<Inventory<T>>> providing = inventories.getRight();
		final List<Positioned<Inventory<T>>> receiving = inventories.getLeft();
		providing.sort((x, y) -> Integer.compare(x.value().getSettings().getProvidePriority(), y.value().getSettings().getProvidePriority()));
		providing.forEach(providingInv -> {
			final List<T> items = providingInv.value().nextAvailable();
			if(items == null || items.isEmpty())
				return;
			final Distributor<T, Inventory<T>> distributor = providingInv.value().getSettings().getDestinationMode().getFactory().create(this, providingInv);
			distributor.distribute(receiving, items);
		});
	}

	@Override
	public String toString() {
		return new StringBuilder("ConduitNetwork[Type: ").append(this.type.getRegistryName().getPath())
				.append(", Graph: ").append(this.graph).append("]").toString();
	}

}
