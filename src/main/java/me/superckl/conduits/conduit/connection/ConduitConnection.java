package me.superckl.conduits.conduit.connection;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import com.google.common.base.Objects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import lombok.Getter;
import me.superckl.conduits.ModConduits;
import me.superckl.conduits.common.block.ConduitBlockEntity;
import me.superckl.conduits.conduit.ConduitType;
import me.superckl.conduits.conduit.network.inventory.TransferrableQuantity;
import me.superckl.conduits.util.WarningHelper;
import net.minecraft.core.Direction;

@Getter
public abstract class ConduitConnection {

	public static final Codec<ConduitConnection> CODEC =  Identifier.CODEC
			.dispatch(ConduitConnection::getIdentifier, id -> id.type().getCodec(id.connectionType()).fieldOf("value"));

	private final ConduitType<?> type;
	private final Identifier identifier;

	protected ConduitConnection(final ConduitType<?> type) {
		this.type = type;
		this.identifier = new Identifier(this.getConnectionType(), type);
	}

	public ConduitConnectionType getConnectionType() {
		if(this instanceof Conduit)
			return ConduitConnectionType.CONDUIT;
		if(this instanceof Inventory)
			return ConduitConnectionType.INVENTORY;
		throw new IncompatibleClassChangeError();
	}

	public final Inventory<?> asInventory() {
		return (Inventory<?>) this;
	}

	@Override
	public boolean equals(final Object obj) {
		if(obj instanceof final ConduitConnection conn)
			return Objects.equal(this.type, conn.type) && Objects.equal(this.getConnectionType(), conn.getConnectionType());
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.type, this.getConnectionType());
	}

	/**
	 * Copies this conduit connection for placing into a map as a key.
	 * The returned value should not contain any references to external structure
	 * such as block entities since these will not be able to be garbage collected.
	 */
	public abstract ConduitConnection copyForMap();

	public boolean resolve() {return true;}

	public static class Conduit extends ConduitConnection{

		public static Codec<Conduit> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				ModConduits.TYPES_CODEC.fieldOf("conduitType").forGetter(Conduit::getType))
				.apply(instance, Conduit::new));

		public Conduit(final ConduitType<?> type) {
			super(type);
		}

		@Override
		public ConduitConnection copyForMap() {
			return new Conduit(this.getType());
		}

	}

	public static abstract class Inventory<T extends TransferrableQuantity> extends ConduitConnection implements Consumer<T>{

		public static final Comparator<Inventory<?>> ACCEPT_PRIORITY_COMPARATOR = (x, y) ->
		Integer.compare(x.getSettings().getAcceptPriority(), y.getSettings().getAcceptPriority());

		public static final Comparator<Inventory<?>> PROVIDE_PRIORITY_COMPARATOR = (x, y) ->
		Integer.compare(x.getSettings().getProvidePriority(), y.getSettings().getProvidePriority());

		protected ConduitBlockEntity owner;
		protected final Direction fromDir;

		@Getter
		private final InventoryConnectionSettings settings;

		protected Inventory(final ConduitType<T> type, final Direction fromConduit, final InventoryConnectionSettings settings) {
			super(type);
			this.fromDir = fromConduit;
			this.settings = settings.copy(this::onValueChange);
		}

		public void invalidate() {
			if(!this.owner.isRemoved())
				this.owner.inventoryInvalidated(this.fromDir, this);
		}

		@Override
		public ConduitConnection copyForMap() {
			return new DummyInventoryConnection<>(this.getType());
		}

		protected void onValueChange(final InventoryConnectionSettings.Setting setting) {
			this.owner.settingsChange();
		}

		public Inventory<T> setOwner(final ConduitBlockEntity owner) {
			if(this.owner != null)
				throw new IllegalStateException("Connection already has owner!");
			this.owner = owner;
			return this;
		}

		/**
		 * Get a list of the next available set of quantities. This will only be called
		 * once per transfer operation, so handlers that only provide portions of inventories
		 * can safely move to the next portion here.
		 * If there are no items to be transferred (i.e., all are already consumed), this should
		 * return an empty list to avoid calculation of destination priorities.
		 */
		public abstract List<T> nextAvailable();
		@Override
		public abstract void accept(T quantity);

		/**
		 * Called once a tick before the network attempts to send items to this connection
		 * Can be used to cache information that may change between ticks but should be
		 * recomputed for every insertion attempt (e.g., open slots, items in slots)
		 */
		public void setupReceivingCache() {}

		/**
		 * Convenience method to restore the generic of this connection type when the type generic is known
		 */
		public final <V extends TransferrableQuantity> Inventory<V> restoreGeneric(final ConduitType<V> type) {
			if(this.getType() != type)
				throw new IllegalArgumentException("Cannot change connection to a different type!");
			return WarningHelper.uncheckedCast(this);
		}

		@Override
		public boolean equals(final Object obj) {
			if(obj instanceof final Inventory<?> inv)
				return this.fromDir == inv.fromDir && super.equals(obj);
			return false;
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(this.getType(), this.getConnectionType(), this.fromDir);
		}

	}

	private static class DummyInventoryConnection<T extends TransferrableQuantity> extends Inventory<T>{

		protected DummyInventoryConnection(final ConduitType<T> type) {
			super(type, null, new InventoryConnectionSettings());
		}

		@Override
		public ConduitConnection copyForMap() {
			return new DummyInventoryConnection<>(this.getType());
		}

		@Override
		public InventoryConnectionSettings getSettings() {
			throw new UnsupportedOperationException();
		}

		@Override
		protected void onValueChange(final InventoryConnectionSettings.Setting setting) {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<T> nextAvailable() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void accept(final T quantity) {
			throw new UnsupportedOperationException();
		}

	}

	private record Identifier(ConduitConnectionType connectionType, ConduitType<?> type) {

		private static final Codec<Identifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				ConduitConnectionType.CODEC.fieldOf("connectionType").forGetter(Identifier::connectionType),
				ModConduits.TYPES_CODEC.fieldOf("conduitType").forGetter(Identifier::type))
				.apply(instance, Identifier::new));

	}

}
