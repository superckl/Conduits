package me.superckl.conduits.common.block;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import me.superckl.conduits.Conduits;
import me.superckl.conduits.ModBlocks;
import me.superckl.conduits.common.item.ConduitItem;
import me.superckl.conduits.conduit.ConduitTier;
import me.superckl.conduits.conduit.ConduitType;
import me.superckl.conduits.conduit.connection.ConduitConnection;
import me.superckl.conduits.conduit.connection.ConduitConnectionMap;
import me.superckl.conduits.conduit.connection.ConduitConnectionType;
import me.superckl.conduits.conduit.network.ConduitNetwork;
import me.superckl.conduits.conduit.network.inventory.TransferrableQuantity;
import me.superckl.conduits.util.WarningHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ConduitBlockEntity extends BlockEntity {

    @Getter
    private final ConduitConnectionMap connections = ConduitConnectionMap.make();
    private final Map<ConduitType<?>, ConduitNetwork<?>> networks = new Object2ObjectOpenHashMap<>();

    public ConduitBlockEntity(final BlockPos pWorldPosition, final BlockState pBlockState) {
        super(ModBlocks.CONDUIT_ENTITY.get(), pWorldPosition, pBlockState);
    }

    /**
     * Should be called by the conduit item when it places a block.
     * This sets the initial state of the block entity since it starts empty (which is invalid)
     *
     * @param placer
     */
    public void onPlaced(final ConduitItem placer) {
        this.connections.setTier(placer.getType().get(), placer.getTier());
        this.discoverNeighbors();
        this.connectionChange();
        ConduitNetwork.mergeOrEstablish(this);
    }

    public void onNeighborChanged(final Direction dir, final Block block) {
        this.tryConnect(dir, block);
    }

    /**
     * Checks if this block entity has the given conduit type
     */
    public boolean hasType(final ConduitType<?> type) {
        return this.connections.hasType(type);
    }

    /**
     * Checks if this block entity has the passed tier for the given type.
     */
    public boolean isTier(final ConduitType<?> type, final ConduitTier tier) {
        return this.connections.getTier(type) == tier;
    }

    public ConduitTier getTier(final ConduitType<?> type) {
        return this.connections.getTier(type);
    }

    public <T extends TransferrableQuantity> Optional<ConduitNetwork<T>> getNetwork(final ConduitType<T> type) {
        return Optional.ofNullable(WarningHelper.uncheckedCast(this.networks.get(type)));
    }

    public <T extends TransferrableQuantity> ConduitNetwork<T> setNetwork(final ConduitType<T> type, final ConduitNetwork<T> network) {
        return WarningHelper.uncheckedCast(this.networks.put(type, network));
    }

    /**
     * Attempts to set the conduit tier of this block entity.
     *
     * @param type The type to set the tier of.
     * @param tier The tier to set the type to.
     * @return If the set failed, an optional containing the passed tier will be returned. Otherwise, the
     * optional will contain the previous tier of the type (possibly empty)
     */
    public Optional<ConduitTier> trySetTier(final ConduitType<?> type, final ConduitTier tier) {
        if (this.isTier(type, tier))
            return Optional.of(tier);
        final ConduitTier prev = this.connections.setTier(type, tier);
        this.connectionChange();
        if (prev == null)
            this.discoverNeighbors();
        //TODO update network on server side only
        return Optional.ofNullable(prev);
    }

    public boolean removeType(final ConduitType<?> type) {
        final boolean changed = this.connections.removeConnections(type);
        if (changed)
            this.connectionChange();
        return changed;
    }

    /**
     * Examines all directions to determine and establish (or remove) connections.
     */
    private void discoverNeighbors() {
        for (final Direction dir : Direction.values()) {
            final BlockPos pos = this.worldPosition.relative(dir);
            if (!this.level.isLoaded(pos))
                this.removeConnections(dir);
            this.tryConnect(dir, this.level.getBlockState(pos).getBlock());
        }
    }

    /**
     * Attempts to establish or remove connections. Marks the block entity dirty if a connection was established (or removed)
     *
     * @param dir   The direction in which to check connections
     * @param block The blockstate that exists in that direction
     */
    private void tryConnect(final Direction dir, final Block block) {
        if (!(block instanceof EntityBlock)) {
            this.removeConnections(dir);
            return;
        }
        final BlockEntity be = this.level.getBlockEntity(this.worldPosition.relative(dir));
        this.connections.types().forEach(type -> {
            if (be instanceof final ConduitBlockEntity conduit) {
                final ConduitTier tier = this.connections.getTier(type);
                if (conduit.hasType(type) && conduit.isTier(type, tier))
                    this.makeConnection(type, dir, ConduitConnectionType.CONDUIT);
                else
                    this.removeConnection(type, dir);
            } else if (type.canConnect(dir, be))
                this.makeConnection(type, dir, ConduitConnectionType.INVENTORY);
            else
                this.removeConnection(type, dir);
        });
    }

    public Collection<ConduitBlockEntity> getConnectedConduits(final ConduitType<?> type) {
        return this.connections.getConnections(type).entrySet().stream()
                .filter(entry -> entry.getValue().getConnectionType() == ConduitConnectionType.CONDUIT).map(Map.Entry::getKey)
                .map(this::getNeighborConduit).collect(Collectors.toList());
    }

    public <T extends TransferrableQuantity> Collection<ConduitConnection.Inventory<T>> getConnectedInventories(final ConduitType<T> type) {
        return this.connections.getConnections(type).entrySet().stream()
                .filter(entry -> entry.getValue().getConnectionType() == ConduitConnectionType.INVENTORY).map(Map.Entry::getValue)
                .map(con -> con.asInventory().restoreGeneric(type)).collect(Collectors.toList());
    }

    private ConduitBlockEntity getNeighborConduit(final Direction dir) {
        final BlockPos pos = this.worldPosition.relative(dir);
        if (!this.level.isLoaded(pos))
            return null;
        return this.level.getBlockEntity(pos, ModBlocks.CONDUIT_ENTITY.get()).orElse(null);
    }

    public <T extends TransferrableQuantity> Optional<ConduitConnection.Inventory<T>> establishInventoryConnection(final ConduitType<T> type,
                                                                                                                   final Direction dir) {
        if (!this.getConnections().hasConnection(type, dir))
            return Optional.empty();
        final BlockEntity inventory = this.level.getBlockEntity(this.worldPosition.relative(dir));
        if (inventory == null || !type.canConnect(dir.getOpposite(), inventory)) {
            Conduits.LOG.warn("Conduit at %s was connected to inventory %s at %s but that inventory is not connectable!",
                    this.worldPosition, inventory == null ? "N/A (null)" : inventory.getType().builtInRegistryHolder().getKey().location(),
                    this.worldPosition.relative(dir));
            this.discoverNeighbors();
            return Optional.empty();
        }

        final ConduitConnection.Inventory<T> connection = type.establishConnection(ConduitConnectionType.INVENTORY, dir, this)
                .asInventory().restoreGeneric(type);
        connection.resolve();
        return Optional.of(connection);
    }

    public void inventoryInvalidated(final Direction dir, final ConduitConnection.Inventory<?> con) {
        if (this.connections.removeConnection(dir, con))
            this.connectionChange();
    }

    private boolean makeConnection(final ConduitType<?> type, final Direction dir, final ConduitConnectionType con) {
        final ConduitConnection connection = type.establishConnection(con, dir, this);
        if (this.connections.makeConnection(type, dir, connection)) {
            connection.resolve();
            this.connectionChange();
            return true;
        }
        return false;
    }

    private void removeConnection(final ConduitType<?> type, final Direction dir) {
        if (this.connections.removeConnection(type, dir))
            this.connectionChange();
    }

    private void removeConnections(final Direction dir) {
        if (this.connections.removeConnections(dir))
            this.connectionChange();
    }

    private void connectionChange() {
        this.settingsChange();
        this.requestModelDataUpdate();

        this.notifyNetworks();
    }

    public void settingsChange() {
        this.setChanged();
        this.sendUpdate();
    }

    private void notifyNetworks() {
        if (!this.level.isClientSide)
            this.connections.types().forEach(type -> this.networks.computeIfAbsent(type,
                    x -> ConduitNetwork.establish(this, x)).connectionChange(this));
    }

    private void sendUpdate() {
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
    }

    @Override
    public void setRemoved() {
        this.networks.values().forEach(network -> network.removed(this));
        this.networks.clear();
        super.setRemoved();
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        //this.connections.resolveConnections();
        this.notifyNetworks();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        this.connections.resolveConnections();
        this.notifyNetworks();
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        // TODO sync only what changed, requires custom packets
        return this.saveWithoutMetadata(pRegistries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        //TODO sync only what changed, requires custom packets
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(final Connection net, final ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider pRegistries) {
        super.onDataPacket(net, pkt, pRegistries);
        if (this.level.isClientSide)
            this.connectionChange();
    }

    private static final String DATA_KEY = "conduit_data";

    @Override
    protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.loadAdditional(pTag, pRegistries);
        if (pTag.contains(ConduitBlockEntity.DATA_KEY, Tag.TAG_COMPOUND)) {
            final CompoundTag conduitData = pTag.getCompound(ConduitBlockEntity.DATA_KEY);
            this.connections.load(conduitData, this);
        }
    }

    @Override
    protected void saveAdditional(final CompoundTag pTag, HolderLookup.Provider pRegistries) {
        pTag.put(ConduitBlockEntity.DATA_KEY, this.connections.serialize());
        super.saveAdditional(pTag, pRegistries);
    }

    //MODEL DATA
    public static final ModelProperty<ConduitConnectionMap> CONNECTION_PROPERTY = new ModelProperty<>();

    @Override
    public @NotNull ModelData getModelData() {
        return ModelData.of(ConduitBlockEntity.CONNECTION_PROPERTY, this.connections);
    }

}
