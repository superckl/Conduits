package me.superckl.conduits.util;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import lombok.AllArgsConstructor;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

@AllArgsConstructor
public class PositionedCache<T> {

    private final ListMultimap<BlockPos, Positioned<T>> lookup = MultimapBuilder.hashKeys().arrayListValues().build();
    private final List<Positioned<T>> values = new ArrayList<>();

    public boolean add(final Positioned<T> value) {
        if (this.values.contains(value))
            return false;
        this.lookup.put(value.pos(), value);
        return this.values.add(value);
    }

    public boolean remove(final Positioned<T> value) {
        this.lookup.remove(value.pos(), value);
        return this.values.remove(value);
    }

    public void removeAll(final BlockPos pos) {
        this.lookup.removeAll(pos).forEach(this.values::remove);
    }

    public void addAll(final PositionedCache<T> other) {
        this.lookup.putAll(other.lookup);
        this.values.addAll(other.values);
    }

    public boolean contains(final BlockPos pos) {
        return this.lookup.containsKey(pos);
    }

    public Stream<Positioned<T>> stream() {
        return this.values.stream();
    }

    public PositionedCache<T> filter(final Predicate<BlockPos> filter) {
        final PositionedCache<T> cache = new PositionedCache<>();
        this.values.stream().filter(positioned -> filter.test(positioned.pos())).forEach(cache::add);
        return cache;
    }

}
