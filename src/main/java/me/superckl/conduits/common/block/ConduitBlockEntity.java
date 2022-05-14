package me.superckl.conduits.common.block;

import java.util.Optional;

import lombok.Getter;
import me.superckl.conduits.ModBlocks;
import me.superckl.conduits.common.item.ConduitItem;
import me.superckl.conduits.conduit.ConduitTier;
import me.superckl.conduits.conduit.ConduitType;
import me.superckl.conduits.conduit.connection.ConduitConnectionMap;
import me.superckl.conduits.conduit.connection.ConduitConnectionType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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

	@Getter
	private final ConduitConnectionMap connections = ConduitConnectionMap.make();

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

	public boolean removeType(final ConduitType type) {
		final boolean changed = this.connections.removeConnections(type);
		if(changed)
			this.connectionChange();
		return changed;
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
		this.connections.types().forEach(type -> {
			if(be instanceof final ConduitBlockEntity conduit) {
				final ConduitTier tier = this.connections.getTier(type);
				if(conduit.hasType(type) && conduit.isTier(type, tier))
					this.setConnection(type, dir, ConduitConnectionType.CONDUIT);
				else
					this.removeConnection(type, dir);
			}else if(type.getConnectionHelper().canConnect(dir.getOpposite(), be))
				this.setConnection(type, dir, ConduitConnectionType.INVENTORY);
			else
				this.removeConnection(type, dir);
		});
	}

	private void setConnection(final ConduitType type, final Direction dir, final ConduitConnectionType con) {
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

	//MODEL DATA
	public static final ModelProperty<ConduitConnectionMap> CONNECTION_PROPERTY = new ModelProperty<>();

	@Override
	public IModelData getModelData() {
		return new ModelDataMap.Builder().withInitial(ConduitBlockEntity.CONNECTION_PROPERTY, this.connections).build();
	}

}
