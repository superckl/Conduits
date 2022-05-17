package me.superckl.conduits.conduit.connection;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.math.IntMath;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.superckl.conduits.common.block.ConduitBlockEntity;
import me.superckl.conduits.conduit.ConduitTier;
import me.superckl.conduits.conduit.ConduitType;
import me.superckl.conduits.util.NBTUtil;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

@RequiredArgsConstructor
@EqualsAndHashCode
@Getter
public class ConduitConnectionState{

	private static final String TIER_KEY = "tier";
	private static final String CONNECTION_KEY = "connection";

	private final ConduitType type;
	private final ConduitTier tier;
	private final Map<Direction, ConduitConnection> connections;

	public CompoundTag serialize() {
		final CompoundTag tag = new CompoundTag();
		tag.putString(ConduitConnectionState.TIER_KEY, this.tier.getSerializedName());
		tag.put(ConduitConnectionState.CONNECTION_KEY, NBTUtil.serializeMap(this.connections, ConduitConnection::tag));
		return tag;
	}

	public boolean makeConnection(final Direction dir, final ConduitConnection conn) {
		return this.connections.putIfAbsent(dir, conn) == null;
	}

	public ConduitConnection removeConnection(final Direction dir) {
		return this.connections.remove(dir);
	}

	public boolean removeConnection(final Direction dir, final ConduitConnection conn) {
		return this.connections.remove(dir, conn);
	}

	public boolean hasConnection(final Direction dir) {
		return this.connections.containsKey(dir);
	}

	public boolean resolveConnections() {
		return this.connections.values().stream().map(ConduitConnection::resolve).allMatch(Boolean::booleanValue);
	}

	public ConduitConnectionState copyForMap() {
		final Map<Direction, ConduitConnection> connections = this.connections.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().copyForMap(),
						(x, j) -> {throw new UnsupportedOperationException();}, () -> new EnumMap<>(Direction.class)));
		return new ConduitConnectionState(this.type, this.tier, connections);
	}

	public static ConduitConnectionState with(final ConduitType type, final ConduitTier tier) {
		return new ConduitConnectionState(type, tier, new EnumMap<>(Direction.class));
	}

	public static int states() {
		final int dirStates = IntMath.pow(ConduitConnectionType.values().length+1, Direction.values().length);
		return ConduitTier.values().length*dirStates;
	}

	public static ConduitConnectionState from(final Tag tag, final ConduitType type, final ConduitBlockEntity owner) {
		if(tag instanceof final CompoundTag comp) {
			final ConduitTier tier = NBTUtil.enumFromString(ConduitTier.class, comp.getString(ConduitConnectionState.TIER_KEY));
			final Map<Direction, ConduitConnection> connections = NBTUtil.deserializeMap(comp.getCompound(ConduitConnectionState.CONNECTION_KEY), () -> new EnumMap<>(Direction.class),
					Direction.class, (fromConduit, typeTag) -> ConduitConnection.fromTag((CompoundTag) typeTag,
							connType -> connType.apply(type, fromConduit, owner)));
			return new ConduitConnectionState(type, tier, connections);
		}
		return null;
	}

}