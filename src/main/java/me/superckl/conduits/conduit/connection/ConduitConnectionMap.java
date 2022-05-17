package me.superckl.conduits.conduit.connection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.math.IntMath;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import it.unimi.dsi.fastutil.booleans.BooleanObjectImmutablePair;
import it.unimi.dsi.fastutil.booleans.BooleanObjectPair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.superckl.conduits.common.block.ConduitBlockEntity;
import me.superckl.conduits.conduit.ConduitShapeHelper;
import me.superckl.conduits.conduit.ConduitTier;
import me.superckl.conduits.conduit.ConduitType;
import me.superckl.conduits.conduit.ConfiguredConduit;
import me.superckl.conduits.conduit.part.ConduitPart;
import me.superckl.conduits.conduit.part.ConduitPartType;
import me.superckl.conduits.util.NBTUtil;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;

public record ConduitConnectionMap(Map<ConduitType, ConduitConnectionState> data){

	public ConduitTier setTier(final ConduitType type, final ConduitTier tier) {
		if(this.data.containsKey(type))
			return this.data.put(type, new ConduitConnectionState(type, tier, this.data.get(type).connections())).tier();
		this.data.put(type, ConduitConnectionState.with(type, tier));
		return null;
	}

	public int numTypes() {
		return this.data.size();
	}

	public boolean hasType(final ConduitType type) {
		return this.data.containsKey(type);
	}

	public ConduitTier getTier(final ConduitType type) {
		return this.hasType(type) ? this.data.get(type).tier() : null;
	}

	public Stream<ConduitType> types(){
		return this.data.keySet().stream();
	}

	public Collection<ConduitType> getTypes(){
		return Collections.unmodifiableCollection(this.data.keySet());
	}

	public int numConnections(final Direction dir) {
		int num = 0;
		for(final ConduitConnectionState state:this.data.values())
			if(state.hasConnection(dir))
				num += 1;
		return num;
	}

	public Map<Direction, ConduitConnection> getConnections(final ConduitType type){
		if(!this.hasType(type))
			return Collections.emptyMap();
		return Collections.unmodifiableMap(this.data.get(type).connections());
	}

	public boolean hasConnection(final ConduitType type, final Direction dir) {
		return this.getConnections(type).containsKey(dir);
	}

	public boolean makeConnection(final ConduitType type, final Direction dir, final ConduitConnection con) {
		if(!this.hasType(type))
			throw new IllegalStateException("Cannot set connection for a missing type! "+type);
		return this.data.get(type).makeConnection(dir, con);
	}

	public boolean removeConnection(final ConduitType type, final Direction dir) {
		if(!this.hasType(type))
			return false;
		return this.data.get(type).removeConnection(dir) != null;
	}

	public boolean removeConnection(final Direction dir, final ConduitConnection conn) {
		if(!this.hasType(conn.getType()))
			return false;
		return this.data.get(conn.getType()).removeConnection(dir, conn);
	}

	/**
	 * Removes all connections in the given direction. Marks the block entity dirty is anything changed.
	 * @param dir The direction in which to remove all connections
	 * @return True if any connections were removed the the block entity marked dirty
	 */
	public boolean removeConnections(final Direction dir) {
		boolean changed = false;
		for(final ConduitConnectionState state :this.data.values())
			changed = state.connections().keySet().removeIf(dir::equals) || changed;
		return changed;
	}

	public boolean removeConnections(final ConduitType type) {
		return this.data.remove(type) != null;
	}

	public boolean resolveConnections() {
		return this.data.values().stream().map(ConduitConnectionState::resolveConnections).allMatch(Boolean::booleanValue);
	}

	public CompoundTag serialize() {
		return NBTUtil.serializeMap(this.data, ConduitConnectionState::serialize);
	}

	public void load(final CompoundTag tag, final ConduitBlockEntity owner) {
		this.data.clear();
		this.data.putAll(NBTUtil.deserializeMap(tag, () -> new EnumMap<>(ConduitType.class), ConduitType.class,
				(type, tag2) -> ConduitConnectionState.from(tag2, type, owner)));
	}

	public Map<Direction, Map<ConduitType, Pair<ConduitTier, ConduitConnection>>> byDirection(){
		final Map<Direction, Map<ConduitType, Pair<ConduitTier, ConduitConnection>>> base = new EnumMap<>(Direction.class);
		this.data.forEach((type, state) -> {
			state.connections().forEach((dir, con) -> {
				base.computeIfAbsent(dir, x -> new EnumMap<>(ConduitType.class)).put(type, Pair.of(state.tier(), con));
			});
		});
		return base;
	}

	//Global cache for connection map -> configured conduit since the computation is expensive
	private static final Map<ConduitConnectionMap, ConfiguredConduit> PARTS_CACHE = ConduitConnectionMap.newConduitCache(true);

	public ConfiguredConduit getParts(){
		return ConduitConnectionMap.PARTS_CACHE.computeIfAbsent(this, ConduitConnectionMap::toParts);
	}

	public BooleanObjectPair<Direction> jointState(){
		final var byDir = this.byDirection();
		boolean mixed = true;
		final Direction passThrough;
		final long numTypes = this.types().count();

		//Determine what kind of joint we have
		if(numTypes <= 1 || byDir.size() == 0) {
			//This is an isolated conduit, not mixed and not passthrough
			mixed = false;
			passThrough = null;
		}else if(byDir.size() == 1) {
			//This is the the end of series of conduits
			final Direction dir = byDir.keySet().iterator().next();
			if(byDir.get(dir).size() == numTypes) {
				//All types present in this conduit have a connection the previous, can do passthrough
				mixed = false;
				passThrough = byDir.keySet().iterator().next();
			}else
				passThrough = null; //A type is missing a connection, fallback to mixed
		}else if(byDir.size() == 2) {
			//This could be a corner or a straight passthrough
			final Direction dir = byDir.keySet().iterator().next();
			if(byDir.get(dir).size() == numTypes && byDir.containsKey(dir.getOpposite()) && ConduitShapeHelper.isPassthrough(byDir.get(dir), byDir.get(dir.getOpposite()))) {
				//This is a straight passthrough
				mixed = false;
				passThrough = dir;
			}else
				passThrough = null; //Either a type is missing a connection or this is a corner, fallback to mixed
		}else
			passThrough = null; //This is a complicated joint, fallback to mixed
		return BooleanObjectImmutablePair.of(mixed, passThrough);
	}

	private static ConfiguredConduit toParts(final ConduitConnectionMap conMap) {
		final var byDir = conMap.byDirection();

		//Segments
		final ListMultimap<Direction, ConduitPart> segments = MultimapBuilder.enumKeys(Direction.class).arrayListValues().build();
		final Map<Direction, ConduitPart> connections = new EnumMap<>(Direction.class);
		byDir.forEach((dir, map) -> {
			//For each direction, add a segment for each type
			final int numCon = map.size();
			final var offsets = ConduitShapeHelper.segmentOffsets(numCon, dir);
			final var types = ConduitShapeHelper.sort(map.keySet());
			for(int i = 0; i < numCon; i++)
				segments.put(dir, new ConduitPart(ConduitPartType.SEGMENT,
						map.get(types[i]).getLeft(), types[i], offsets[i], null,
						ConduitShapeHelper.segmentRotation(dir)));
			if(map.values().stream().map(Pair::getRight).map(ConduitConnection::getConnectionType)
					.anyMatch(ConduitConnectionType.INVENTORY::equals))
				connections.put(dir, new ConduitPart(ConduitPartType.CONNECTION, null, null,
						Vector3f.ZERO, null, ConduitShapeHelper.segmentRotation(dir)));
		});

		final List<ConduitPart> joints = new ArrayList<>();
		ConduitPart mixedJoint = null;
		final BooleanObjectPair<Direction> jointState = conMap.jointState();
		final var types = ConduitShapeHelper.sort(conMap.data.keySet());
		if(jointState.leftBoolean()) {
			final AABB size = ConduitShapeHelper.boundMixedJoint(byDir.entrySet().stream()
					.collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().size(),
							(i, j) -> i + j, () -> new EnumMap<>(Direction.class))));
			mixedJoint = new ConduitPart(ConduitPartType.MIXED_JOINT, null, null, Vector3f.ZERO,
					size,  Quaternion.ONE);
		}else {
			final var offsets = ConduitShapeHelper.segmentOffsets(types.length, jointState.right());
			for(int i = 0; i < types.length; i++)
				joints.add(new ConduitPart(ConduitPartType.JOINT, conMap.data.get(types[i]).tier(),
						types[i], offsets[i], null, ConduitShapeHelper.segmentRotation(jointState.right())));
		}
		return new ConfiguredConduit(types, joints, mixedJoint, connections, segments);
	}

	public static ConduitConnectionMap make() {
		return new ConduitConnectionMap(new EnumMap<>(ConduitType.class));
	}

	public static int states() {
		return IntMath.pow(ConduitConnectionState.states()+1, ConduitType.values().length);
	}

	public static <K, V> Map<K, V> newConduitCache(final boolean threadSafe){
		final int capacity = 2^16;
		return threadSafe ? new ConcurrentHashMap<>(capacity) : new Object2ObjectOpenHashMap<>(capacity);
	}

}