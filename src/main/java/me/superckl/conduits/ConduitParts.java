package me.superckl.conduits;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public record ConduitParts<T>(Map<ConduitType, T> joints, Map<ConduitType, T> segments, T inventoryConnection, T mixedJoint){

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
	}

	public List<T> all(){
		final List<T> list = new ArrayList<>(this.joints.values());
		list.addAll(this.segments.values());
		list.add(this.inventoryConnection);
		list.add(this.mixedJoint);
		return list;
	}

	public static <T> ConduitParts<T> from(final BiFunction<PartType, ConduitType, ? extends T> serializer) {
		final Map<ConduitType, T> joints = new EnumMap<>(ConduitType.class);
		final Map<ConduitType, T> segments = new EnumMap<>(ConduitType.class);
		for(final ConduitType type:ConduitType.values()) {
			joints.put(type, serializer.apply(PartType.JOINT, type));
			segments.put(type, serializer.apply(PartType.SEGMENT, type));
		}

		final T inventoryConnection = serializer.apply(PartType.CONNECTION, null);
		final T mixedJoint = serializer.apply(PartType.MIXED_JOINT, null);

		return new ConduitParts<>(Collections.unmodifiableMap(joints), Collections.unmodifiableMap(segments), inventoryConnection, mixedJoint);
	}

}