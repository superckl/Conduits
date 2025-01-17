package me.superckl.conduits.util;

import net.minecraft.core.BlockPos;

import java.util.Comparator;

public record Positioned<T>(BlockPos pos, T value) {

    public static <T> Comparator<Positioned<T>> valueComparator(final Comparator<T> compare) {
        return (x, y) -> compare.compare(x.value, y.value);
    }

    public static <T> Comparator<Positioned<T>> posComparator(final Comparator<BlockPos> compare) {
        return (x, y) -> compare.compare(x.pos, y.pos);
    }

}
