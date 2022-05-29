package me.superckl.conduits.conduit.connection;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.math.IntMath;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import me.superckl.conduits.ModConduits;
import me.superckl.conduits.common.block.ConduitBlockEntity;
import me.superckl.conduits.conduit.ConduitTier;
import me.superckl.conduits.conduit.ConduitType;
import me.superckl.conduits.util.ConduitUtil;
import net.minecraft.core.Direction;

@EqualsAndHashCode
@Getter
public class ConduitConnectionState{

	public static final Codec<ConduitConnectionState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ModConduits.TYPES_CODEC.fieldOf("conduitType").forGetter(ConduitConnectionState::getType),
			ConduitTier.CODEC.fieldOf("tier").forGetter(ConduitConnectionState::getTier),
			ConduitUtil.generalizedMapCodec(Direction.CODEC, ConduitConnection.CODEC,
					() -> new EnumMap<>(Direction.class)).fieldOf("connections").forGetter(ConduitConnectionState::getConnections))
			.apply(instance, ConduitConnectionState::new));

	private final ConduitType<?> type;
	private final ConduitTier tier;
	private final Map<Direction, ConduitConnection> connections;

	public ConduitConnectionState(final ConduitType<?> type, final ConduitTier tier, final Map<Direction, ConduitConnection> connections) {

		this.type = type;
		this.tier = tier;
		this.connections = new EnumMap<>(Direction.class);
		this.connections.putAll(connections);
	}

	public void setOwners(final ConduitBlockEntity owner) {
		this.connections.values().stream().filter(conn -> conn.getConnectionType() == ConduitConnectionType.INVENTORY)
		.map(ConduitConnection::asInventory).forEach(inv -> inv.setOwner(owner));
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

	public static ConduitConnectionState with(final ConduitType<?> type, final ConduitTier tier) {
		return new ConduitConnectionState(type, tier, new EnumMap<>(Direction.class));
	}

	public static int states() {
		final int dirStates = IntMath.pow(ConduitConnectionType.values().length+1, Direction.values().length);
		return ConduitTier.values().length*dirStates;
	}

}