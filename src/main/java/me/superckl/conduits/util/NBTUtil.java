package me.superckl.conduits.util;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.StringRepresentable;

public class NBTUtil {

	public static <K extends Enum<K> & StringRepresentable, V> CompoundTag serializeMap(final Map<K, V> map, final Function<? super V, ? extends Tag> serializer) {
		final CompoundTag tag = new CompoundTag();
		map.forEach((key, value) -> tag.put(key.getSerializedName(), serializer.apply(value)));
		return tag;
	}

	public static <K extends Enum<K> & StringRepresentable, V> Map<K,V> deserializeMap(final CompoundTag tag,
			final Supplier<? extends Map<K, V>> mapMaker, final Class<K> clazz, final Function<? super Tag, ? extends V> deserializer){
		final Map<K, V> map = mapMaker.get();
		tag.getAllKeys().forEach(key -> map.put(NBTUtil.enumFromString(clazz, key), deserializer.apply(tag.get(key))));
		return map;
	}

	public static <K extends Enum<K> & StringRepresentable, V> Map<K,V> deserializeMap(final CompoundTag tag,
			final Supplier<? extends Map<K, V>> mapMaker, final Class<K> clazz, final BiFunction<K, ? super Tag, ? extends V> deserializer){
		final Map<K, V> map = mapMaker.get();
		tag.getAllKeys().forEach(key -> {
			final K enumKey = NBTUtil.enumFromString(clazz, key);
			map.put(enumKey, deserializer.apply(enumKey, tag.get(key)));
		});
		return map;
	}

	public static <K extends Enum<K> & StringRepresentable> K enumFromString(final Class<K> clazz, final String value){
		for(final K e:clazz.getEnumConstants())
			if (e.getSerializedName().equals(value))
				return e;
		return null;
	}

	public static <T, V> V encode(final DynamicOps<? extends V> ops, final T data, final Codec<? super T> codec) {
		final var result = codec.encodeStart(ops, data);
		result.ifError(err -> {throw new IllegalArgumentException(String.format("Failed to encode: %s", err.message()));});
		return result.getOrThrow();
	}

	public static <T, V> T decode(final DynamicOps<V> ops, final V data, final Codec<? extends T> codec) {
		final var result = codec.parse(ops, data);
		result.ifError(err -> {throw new IllegalArgumentException(String.format("Failed to decode: %s", err.message()));});
		return result.getOrThrow();
	}

}
