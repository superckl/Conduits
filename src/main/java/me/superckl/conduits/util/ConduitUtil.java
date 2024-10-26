package me.superckl.conduits.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.io.IOUtils;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.floats.FloatFloatPair;
import me.superckl.conduits.conduit.connection.ConduitConnectionMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ConduitUtil {

	/**
	 * Parses a given resource into a Json element and closes the resource
	 */
	public static JsonElement toJson(final Resource resource) throws IOException {

		try(BufferedReader reader = resource.openAsReader()) {
			return JsonParser.parseReader(reader);
		}
	}

	public static AABB rotateModelAABB(final AABB box, final Quaternionf rotation) {
		final Vector3f bottom = new Vector3f((float) box.minX, (float) box.minY, (float) box.minZ);
		final Vector3f top = new Vector3f((float) box.maxX, (float) box.maxY, (float) box.maxZ);
		final Vector3f rotCenter = new Vector3f(0.5F, 0.5F, 0.5F);

		bottom.sub(rotCenter);
		top.sub(rotCenter);

		bottom.rotate(rotation);
		top.rotate(rotation);

		bottom.add(rotCenter);
		top.add(rotCenter);

		final Vector3f newBottom = new Vector3f(Math.min(bottom.x(), top.x()), Math.min(bottom.y(), top.y()), Math.min(bottom.z(), top.z()));
		final Vector3f newTop = new Vector3f(Math.max(bottom.x(), top.x()), Math.max(bottom.y(), top.y()), Math.max(bottom.z(), top.z()));

		return new AABB(new Vec3(newBottom), new Vec3(newTop));
	}

	public static boolean containsInclusive(final AABB box, final Vec3 loc) {
		return loc.x >= box.minX && loc.x <= box.maxX && loc.y >= box.minY && loc.y <= box.maxY && loc.z >= box.minZ && loc.z <= box.maxZ;
	}

	public static FloatFloatPair max(final FloatFloatPair pair1, final FloatFloatPair pair2) {
		return FloatFloatPair.of(Math.max(pair1.firstFloat(), pair2.firstFloat()),
				Math.max(pair1.secondFloat(), pair2.secondFloat()));
	}

	public static <T> T removeOne(final Collection<T> collection){
		if(collection.isEmpty())
			throw new IllegalArgumentException("Cannot remove element from empty collection!");
		final Iterator<T> it = collection.iterator();
		final T el = it.next();
		it.remove();
		return el;
	}

	public static <T> T getOne(final Collection<T> collection){
		if(collection.isEmpty())
			throw new IllegalArgumentException("Cannot remove element from empty collection!");
		return collection.iterator().next();
	}

	public static <V> V copyComputeIfAbsent(final Map<ConduitConnectionMap, V> map, ConduitConnectionMap key,
			final Function<ConduitConnectionMap, V> computer){
		if(!map.containsKey(key)) {
			key = key.copyForMap();
			final V value = computer.apply(key);
			map.put(key, value);
			return value;
		}
		return map.get(key);
	}

	public static Vec3 localizeHit(final Vec3 hit, final BlockPos blockPos) {
		return hit.subtract(blockPos.getX(), blockPos.getY(), blockPos.getZ());
	}

	public static <K, V> Codec<Map<K, V>> generalizedMapCodec(final Codec<K> keyCodec, final Codec<V> valueCodec, final Supplier<? extends Map<K, V>> mapMaker){
		return MapCodecHelper.makeCodec(keyCodec, valueCodec).comapFlatMap(helper -> helper.toMap(mapMaker), MapCodecHelper::fromMap);
	}

	private static record MapCodecHelper<K, V>(List<K> keys, List<V> values){

		private DataResult<Map<K, V>> toMap(final Supplier<? extends Map<K, V>> mapMaker){
			if(this.keys.size() != this.values.size())
				return DataResult.error(() -> String.format("Different number of keys %d and values %d!", this.keys.size(), this.values.size()));
			final Map<K, V> map = mapMaker.get();
			for (int i = 0; i < this.keys.size(); i++)
				map.put(this.keys.get(i), this.values.get(i));
			return DataResult.success(map);
		}

		private static <K, V> MapCodecHelper<K, V> fromMap(final Map<K, V> map){
			return new MapCodecHelper<>(ImmutableList.copyOf(map.keySet()), ImmutableList.copyOf(map.values()));
		}

		private static <K, V> Codec<MapCodecHelper<K, V>> makeCodec(final Codec<K> keyCodec, final Codec<V> valueCodec){
			return RecordCodecBuilder.create(instance -> instance.group(
					keyCodec.listOf().fieldOf("keys").forGetter(MapCodecHelper::keys),
					valueCodec.listOf().fieldOf("values").forGetter(MapCodecHelper::values))
					.apply(instance, MapCodecHelper::new));
		}
	}

}
