package me.superckl.conduits.conduit.connection;

import java.util.Comparator;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;

import com.google.common.graph.Graph;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.RequiredArgsConstructor;
import me.superckl.conduits.conduit.connection.ConduitConnection.Inventory;
import me.superckl.conduits.conduit.network.ConduitNetwork;
import me.superckl.conduits.conduit.network.Distributor;
import me.superckl.conduits.conduit.network.inventory.TransferrableQuantity;
import me.superckl.conduits.util.GraphUtil;
import me.superckl.conduits.util.Positioned;
import net.minecraft.core.BlockPos;

public abstract class CachedSorter<T> implements Comparator<T>{

	private final Object2IntMap<T> values = new Object2IntOpenHashMap<>();

	protected abstract int computeValue(T value);

	@Override
	public int compare(final T o1, final T o2) {
		return Integer.compare(this.values.computeIfAbsent(o1, (ToIntFunction<T>) this::computeValue),
				this.values.computeIfAbsent(o2, (ToIntFunction<T>) this::computeValue));
	}

	public static class RandomSorter<T> extends CachedSorter<T>{

		private final Random random = new Random();

		@Override
		protected int computeValue(final T value) {
			return this.random.nextInt();
		}

		public static <T extends TransferrableQuantity, V extends Consumer<T>> Distributor<T, V> makeDistributor(final ConduitNetwork<T> network, final Positioned<V> provider){
			return new Distributor.SimplePriorityDistributor<>(new RandomSorter<>());
		}

	}

	public static class AcceptPrioritySorter<T extends TransferrableQuantity, V extends Inventory<T>> extends CachedSorter<Positioned<V>>{

		@Override
		protected int computeValue(final Positioned<V> value) {
			return value.value().getSettings().getAcceptPriority();
		}

		public static <T extends TransferrableQuantity, V extends Inventory<T>> Distributor<T, V> makeDistributor(final ConduitNetwork<T> network, final Positioned<V> provider){
			return new Distributor.SimplePriorityDistributor<>(new AcceptPrioritySorter<>());
		}

	}

	@RequiredArgsConstructor
	public static class DistanceSorter<T> extends CachedSorter<Positioned<T>>{

		private final Object2IntMap<BlockPos> distances;

		public DistanceSorter(final BlockPos origin, final Graph<BlockPos> graph) {
			this.distances = GraphUtil.distances(graph, origin);
		}

		@Override
		protected int computeValue(final Positioned<T> value) {
			return this.distances.getOrDefault(value.pos(), Integer.MAX_VALUE);
		}

		public static <T extends TransferrableQuantity, V extends Inventory<T>> Distributor<T, V> makeDistributor(final ConduitNetwork<T> network, final Positioned<V> provider){
			return new Distributor.SimplePriorityDistributor<>(new DistanceSorter<>(network.computeDistanceMap(provider.pos())));
		}

		@Override
		public int compare(final Positioned<T> o1, final Positioned<T> o2) {
			return super.compare(o2, o1);
		}

	}

}
