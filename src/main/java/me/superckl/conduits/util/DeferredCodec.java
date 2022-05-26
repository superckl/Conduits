package me.superckl.conduits.util;

import java.util.function.Supplier;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeferredCodec<V> implements Codec<V>{

	private final Supplier<Codec<V>> codec;

	@Override
	public <T> DataResult<Pair<V, T>> decode(final DynamicOps<T> ops, final T input){
		return this.codec.get().decode(ops, input);
	}

	@Override
	public <T> DataResult<T> encode(final V input, final DynamicOps<T> ops, final T prefix){
		return this.codec.get().encode(input, ops, prefix);
	}
}
