package me.superckl.conduits.conduit.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.Consumer;

import lombok.RequiredArgsConstructor;
import me.superckl.conduits.conduit.network.inventory.TransferrableQuantity;
import me.superckl.conduits.util.Positioned;

public interface Distributor<T, V extends Consumer<T>> {

	void distribute(Collection<Positioned<V>> receivers, Collection<T> items);

	@RequiredArgsConstructor
	public static class SimplePriorityDistributor<T extends TransferrableQuantity, V extends Consumer<T>> implements Distributor<T, V>{

		private final Comparator<Positioned<V>> comparator;

		@Override
		public void distribute(final Collection<Positioned<V>> receivers, final Collection<T> items) {
			if(receivers.isEmpty())
				return;
			final List<T> itemsCopy = new ArrayList<>(items);
			final PriorityQueue<Positioned<V>> queue = new PriorityQueue<>(receivers.size(), Collections.reverseOrder(this.comparator));
			queue.addAll(receivers);
			while(!queue.isEmpty()) {
				final Positioned<V> pos = queue.poll();
				final Iterator<T> it = itemsCopy.iterator();
				while(it.hasNext()) {
					final T item = it.next();
					pos.value().accept(item);
					if(item.isConsumed())
						it.remove();
				}
			}
		}

	}

}
