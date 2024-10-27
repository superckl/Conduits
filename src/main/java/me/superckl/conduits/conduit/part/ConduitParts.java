package me.superckl.conduits.conduit.part;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.function.Function;

public record ConduitParts<T>(T joint, T segment, T inventoryConnection, T mixedJoint) {

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

    public T get(final ConduitPartType type) {
        return switch (type) {
            case CONNECTION -> this.inventoryConnection;
            case JOINT -> this.joint;
            case MIXED_JOINT -> this.mixedJoint;
            case SEGMENT -> this.segment;
            default -> throw new IllegalArgumentException("No part for type " + type.getSerializedName());
        };
    }

    public List<T> all() {
        final List<T> list = Lists.newArrayList(this.joint);
        list.add(this.segment);
        list.add(this.inventoryConnection);
        list.add(this.mixedJoint);
        return list;
    }

    public static <T> ConduitParts<T> from(final Function<String, ? extends T> serializer, final Class<T> clazz) {
		/*final T[] joints = WarningHelper.uncheckedCast(Array.newInstance(clazz, ConduitType.values().length));
		final T[] segments = WarningHelper.uncheckedCast(Array.newInstance(clazz, ConduitType.values().length));
		for(int i = 0; i < segments.length; i++) {
			joints[i] = serializer.apply(PartType.JOINT.path(null)+"_"+(i+1));
			segments[i] = serializer.apply(PartType.SEGMENT.path(null)+"_"+(i+1));
		}*/

        final T joint = serializer.apply(ConduitPartType.JOINT.path(null));
        final T segment = serializer.apply(ConduitPartType.SEGMENT.path(null));
        final T inventoryConnection = serializer.apply(ConduitPartType.CONNECTION.path(null));
        final T mixedJoint = serializer.apply(ConduitPartType.MIXED_JOINT.path(null));

        return new ConduitParts<>(joint, segment, inventoryConnection, mixedJoint);
    }

}