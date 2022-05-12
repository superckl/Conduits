package me.superckl.conduits.common.block;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.math.IntMath;

import lombok.RequiredArgsConstructor;
import me.superckl.conduits.ConduitTier;
import me.superckl.conduits.ConduitType;
import me.superckl.conduits.ModBlocks;
import me.superckl.conduits.common.item.ConduitItem;
import me.superckl.conduits.util.NBTUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class ConduitBlockEntity extends BlockEntity{

	private final ConnectionData connections = ConnectionData.make();

	public ConduitBlockEntity(final BlockPos pWorldPosition, final BlockState pBlockState) {
		super(ModBlocks.CONDUIT_ENTITY.get(), pWorldPosition, pBlockState);
	}

	/**
	 * Should be called by the conduit item when it places a block.
	 * This sets the initial state of the block entity since it starts empty (which is invalid)
	 * @param placer
	 */
	public void onPlaced(final ConduitItem placer) {
		this.connections.setTier(placer.getType(), placer.getTier());
		this.discoverNeighbors();
		this.connectionChange();
	}

	public void onNeighborChanged(final Direction dir, final Block block) {
		this.tryConnect(dir, block);
	}

	/**
	 * Checks if this block entity has the given conduit type
	 */
	public boolean hasType(final ConduitType type) {
		return this.connections.hasType(type);
	}

	/**
	 * Checks if this block entity has the passed tier for the given type.
	 */
	public boolean isTier(final ConduitType type, final ConduitTier tier) {
		return this.connections.getTier(type) == tier;
	}

	public ConduitTier getTier(final ConduitType type) {
		return this.connections.getTier(type);
	}

	/**
	 * Attempts to set the conduit tier of this block entity.
	 * @param type The type to set the tier of.
	 * @param tier The tier to set the type to.
	 * @return If the set failed, an optional containing the passed tier will be returned. Otherwise, the
	 * optional will contain the previous tier of the type (possibly empty)
	 */
	public Optional<ConduitTier> trySetTier(final ConduitType type, final ConduitTier tier) {
		if(this.isTier(type, tier))
			return Optional.of(tier);
		final ConduitTier prev = this.connections.setTier(type, tier);
		this.connectionChange();
		if(prev == null)
			this.discoverNeighbors();
		//TODO update network on server side only
		return Optional.ofNullable(prev);
	}

	/**
	 * Examines all directions to determine and establish (or remove) connections.
	 */
	private void discoverNeighbors() {
		for(final Direction dir:Direction.values()) {
			final BlockPos pos = this.worldPosition.relative(dir);
			if(!this.level.isLoaded(pos))
				this.removeConnections(dir);
			this.tryConnect(dir, this.level.getBlockState(pos).getBlock());
		}
	}

	/**
	 * Attempts to establish or remove connections. Marks the block entity dirty if a connection was established (or removed)
	 * @param dir The direction in which to check connections
	 * @param state The blockstate that exists in that direction
	 */
	private void tryConnect(final Direction dir, final Block block){
		if(!(block instanceof EntityBlock)) {
			this.removeConnections(dir);
			return;
		}
		final BlockEntity be = this.level.getBlockEntity(this.worldPosition.relative(dir));
		if(be instanceof final ConduitBlockEntity conduit)
			this.connections.types().forEach(type -> {
				final ConduitTier tier = this.connections.getTier(type);
				if(conduit.hasType(type)) {
					if(conduit.isTier(type, tier))
						this.setConnection(type, dir, ConnectionType.CONDUIT);
					else
						this.setConnection(type, dir, ConnectionType.INVENTORY);
				}else
					this.removeConnection(type, dir);
			});
		else
			this.removeConnections(dir);
	}

	private void setConnection(final ConduitType type, final Direction dir, final ConnectionType con) {
		if(this.connections.setConnection(type, dir, con))
			this.connectionChange();
	}

	private void removeConnection(final ConduitType type, final Direction dir) {
		if(this.connections.removeConnection(type, dir))
			this.connectionChange();
	}

	private void removeConnections(final Direction dir) {
		if(this.connections.removeConnections(dir))
			this.connectionChange();
	}

	private void connectionChange() {
		this.setChanged();
		this.sendUpdate();
		this.requestModelDataUpdate();
	}

	private void sendUpdate() {
		this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
	}

	@Override
	public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side) {
		//TODO
		return super.getCapability(cap, side);
	}

	@Override
	public void invalidateCaps() {
		//TODO
		super.invalidateCaps();
	}

	@Override
	public CompoundTag getUpdateTag() {
		// TODO sync only what changed, requires custom packets
		return this.saveWithoutMetadata();
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		//TODO sync only what changed, requires custom packets
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void onDataPacket(final Connection net, final ClientboundBlockEntityDataPacket pkt) {
		super.onDataPacket(net, pkt);
		if(this.level.isClientSide)
			this.connectionChange();
	}

	private static final String DATA_KEY = "conduit_data";
	private static final String TIER_KEY = "tier";
	private static final String CONNECTION_KEY = "connection";

	@Override
	public void load(final CompoundTag pTag) {
		super.load(pTag);
		if(pTag.contains(ConduitBlockEntity.DATA_KEY, Tag.TAG_COMPOUND)) {
			final CompoundTag conduitData = pTag.getCompound(ConduitBlockEntity.DATA_KEY);
			this.connections.load(conduitData);
		}
	}
	@Override
	protected void saveAdditional(final CompoundTag pTag) {
		pTag.put(ConduitBlockEntity.DATA_KEY, this.connections.serialize());
		super.saveAdditional(pTag);
	}

	public static record ConnectionState(ConduitTier tier, Map<Direction, ConnectionType> connections) {

		public CompoundTag serialize() {
			final CompoundTag tag = new CompoundTag();
			tag.putString(ConduitBlockEntity.TIER_KEY, this.tier.getSerializedName());
			tag.put(ConduitBlockEntity.CONNECTION_KEY, NBTUtil.serializeMap(this.connections, ConnectionType::tag));
			return tag;
		}

		public ConnectionType setConnection(final Direction dir, final ConnectionType type) {
			return this.connections.put(dir, type);
		}

		public ConnectionType removeConnection(final Direction dir) {
			return this.connections.remove(dir);
		}

		public boolean hasConnection(final Direction dir) {
			return this.connections.containsKey(dir);
		}

		public static ConnectionState with(final ConduitTier tier) {
			return new ConnectionState(tier, new EnumMap<>(Direction.class));
		}

		public static int states() {
			final int dirStates = IntMath.pow(ConnectionType.values().length+1, Direction.values().length);
			return ConduitTier.values().length*dirStates;
		}

		public static ConnectionState from(final Tag tag) {
			if(tag instanceof final CompoundTag comp) {
				final ConduitTier tier = NBTUtil.enumFromString(ConduitTier.class, comp.getString(ConduitBlockEntity.TIER_KEY));
				final Map<Direction, ConnectionType> connections = NBTUtil.deserializeMap(comp.getCompound(ConduitBlockEntity.CONNECTION_KEY), () -> new EnumMap<>(Direction.class),
						Direction.class, typeTag -> NBTUtil.enumFromString(ConnectionType.class, typeTag.getAsString()));
				return new ConnectionState(tier, connections);
			}
			return null;
		}

	}

	public static record ConnectionData(Map<ConduitType, ConnectionState> data){

		public ConduitTier setTier(final ConduitType type, final ConduitTier tier) {
			if(this.data.containsKey(type))
				return this.data.put(type, new ConnectionState(tier, this.data.get(type).connections())).tier();
			this.data.put(type, ConnectionState.with(tier));
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

		public int numConnections(final Direction dir) {
			int num = 0;
			for(final ConnectionState state:this.data.values())
				if(state.hasConnection(dir))
					num += 1;
			return num;
		}

		public Map<Direction, ConnectionType> getConnections(final ConduitType type){
			if(!this.hasType(type))
				return Collections.emptyMap();
			return Collections.unmodifiableMap(this.data.get(type).connections());
		}

		public boolean hasConnection(final ConduitType type, final Direction dir) {
			return this.getConnections(type).containsKey(dir);
		}

		public boolean setConnection(final ConduitType type, final Direction dir, final ConnectionType con) {
			if(!this.hasType(type))
				return false;
			return this.data.get(type).setConnection(dir, con) != con;
		}

		private boolean removeConnection(final ConduitType type, final Direction dir) {
			if(!this.hasType(type))
				return false;
			return this.data.get(type).removeConnection(dir) != null;
		}

		/**
		 * Removes all connections in the given direction. Marks the block entity dirty is anything changed.
		 * @param dir The direction in which to remove all connections
		 * @return True if any connections were removed the the block entity marked dirty
		 */
		public boolean removeConnections(final Direction dir) {
			boolean changed = false;
			for(final ConnectionState state :this.data.values())
				changed = state.connections().keySet().removeIf(dir::equals) || changed;
			return changed;
		}

		public CompoundTag serialize() {
			return NBTUtil.serializeMap(this.data, ConnectionState::serialize);
		}

		public void load(final CompoundTag tag) {
			this.data.clear();
			this.data.putAll(NBTUtil.deserializeMap(tag, () -> new EnumMap<>(ConduitType.class), ConduitType.class, ConnectionState::from));
		}

		public Map<Direction, Map<ConduitType, Pair<ConduitTier, ConnectionType>>> byDirection(){
			final Map<Direction, Map<ConduitType, Pair<ConduitTier, ConnectionType>>> base = new EnumMap<>(Direction.class);
			this.data.forEach((type, state) -> {
				state.connections().forEach((dir, con) -> {
					base.computeIfAbsent(dir, x -> new EnumMap<>(ConduitType.class)).put(type, Pair.of(state.tier(), con));
				});
			});
			return base;
		}

		public static ConnectionData make() {
			return new ConnectionData(new EnumMap<>(ConduitType.class));
		}

		public static int states() {
			return IntMath.pow(ConnectionState.states()+1, ConduitType.values().length);
		}

	}

	@RequiredArgsConstructor
	public enum ConnectionType implements StringRepresentable{

		CONDUIT("conduit"),
		INVENTORY("inventory");

		private final String name;

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public StringTag tag() {
			return StringTag.valueOf(this.name);
		}
	}

	//MODEL DATA
	public static final ModelProperty<ConnectionData> CONNECTION_PROPERTY = new ModelProperty<>();

	@Override
	public IModelData getModelData() {
		return new ModelDataMap.Builder().withInitial(ConduitBlockEntity.CONNECTION_PROPERTY, this.connections).build();
	}

}
