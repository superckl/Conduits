package me.superckl.conduits;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.google.common.collect.Lists;

import me.superckl.conduits.util.WarningHelper;

public record ConduitParts<T>(T[] joints, T[] segments, T inventoryConnection, T mixedJoint){

	/*
	public JsonObject toJson(final Function<? super T, ? extends JsonElement> serializer) {
		final JsonObject obj = new JsonObject();

		final JsonObject joints = new JsonObject();
		this.joints.forEach((type, model) -> joints.add(type.getSerializedName(), serializer.apply(model)));
		obj.add("joints", joints);

		final JsonObject segments = new JsonObject();
		this.segments.forEach((type, model) -> segments.add(type.getSerializedName(), serializer.apply(model)));
		obj.add("segments", segments);

		obj.add("inventory_connection", serializer.apply(this.inventoryConnection));
		obj.add("mixed_joint", serializer.apply(this.mixedJoint));

		return obj;
	}*/

	public List<T> all(){
		final List<T> list = Lists.newArrayList(this.joints);
		list.addAll(Arrays.asList(this.segments));
		list.add(this.inventoryConnection);
		list.add(this.mixedJoint);
		return list;
	}

	public static <T> ConduitParts<T> from(final Function<String, ? extends T> serializer, final Class<T> clazz) {
		final T[] joints = WarningHelper.uncheckedCast(Array.newInstance(clazz, ConduitType.values().length));
		final T[] segments = WarningHelper.uncheckedCast(Array.newInstance(clazz, ConduitType.values().length));
		for(int i = 0; i < segments.length; i++) {
			joints[i] = serializer.apply(PartType.JOINT.path(null)+"_"+(i+1));
			segments[i] = serializer.apply(PartType.SEGMENT.path(null)+"_"+(i+1));
		}

		final T inventoryConnection = serializer.apply(PartType.CONNECTION.path(null));
		final T mixedJoint = serializer.apply(PartType.MIXED_JOINT.path(null));

		return new ConduitParts<>(joints, segments, inventoryConnection, mixedJoint);
	}

}