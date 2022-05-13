package me.superckl.conduits.conduit.connection;

import java.util.EnumMap;
import java.util.Map;

import com.google.common.math.IntMath;

import me.superckl.conduits.conduit.ConduitTier;
import me.superckl.conduits.util.NBTUtil;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public record ConduitConnectionState(ConduitTier tier, Map<Direction, ConduitConnectionType> connections) {

	private static final String TIER_KEY = "tier";
	private static final String CONNECTION_KEY = "connection";

	public CompoundTag serialize() {
		final CompoundTag tag = new CompoundTag();
		tag.putString(ConduitConnectionState.TIER_KEY, this.tier.getSerializedName());
		tag.put(ConduitConnectionState.CONNECTION_KEY, NBTUtil.serializeMap(this.connections, ConduitConnectionType::tag));
		return tag;
	}

	public ConduitConnectionType setConnection(final Direction dir, final ConduitConnectionType type) {
		return this.connections.put(dir, type);
	}

	public ConduitConnectionType removeConnection(final Direction dir) {
		return this.connections.remove(dir);
	}

	public boolean hasConnection(final Direction dir) {
		return this.connections.containsKey(dir);
	}

	public static ConduitConnectionState with(final ConduitTier tier) {
		return new ConduitConnectionState(tier, new EnumMap<>(Direction.class));
	}

	public static int states() {
		final int dirStates = IntMath.pow(ConduitConnectionType.values().length+1, Direction.values().length);
		return ConduitTier.values().length*dirStates;
	}

	public static ConduitConnectionState from(final Tag tag) {
		if(tag instanceof final CompoundTag comp) {
			final ConduitTier tier = NBTUtil.enumFromString(ConduitTier.class, comp.getString(ConduitConnectionState.TIER_KEY));
			final Map<Direction, ConduitConnectionType> connections = NBTUtil.deserializeMap(comp.getCompound(ConduitConnectionState.CONNECTION_KEY), () -> new EnumMap<>(Direction.class),
					Direction.class, typeTag -> NBTUtil.enumFromString(ConduitConnectionType.class, typeTag.getAsString()));
			return new ConduitConnectionState(tier, connections);
		}
		return null;
	}

}