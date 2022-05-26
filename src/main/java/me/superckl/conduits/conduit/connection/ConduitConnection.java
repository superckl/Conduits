package me.superckl.conduits.conduit.connection;

import java.util.Comparator;

import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import lombok.Getter;
import me.superckl.conduits.ModConduits;
import me.superckl.conduits.common.block.ConduitBlockEntity;
import me.superckl.conduits.conduit.ConduitType;
import net.minecraft.core.Direction;

public abstract class ConduitConnection {

	public static final Codec<ConduitConnection> CODEC =  Identifier.CODEC
			.dispatch(ConduitConnection::getIdentifier, id -> id.type().getCodec(id.connectionType()));

	@Getter
	private final ConduitType type;
	@Getter
	private final Identifier identifier;

	protected ConduitConnection(final ConduitType type) {
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

	public final Inventory asInventory() {
		return (Inventory) this;
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

		protected Conduit(final ConduitType type) {
			super(type);
		}

		@Override
		public ConduitConnection copyForMap() {
			return new Conduit(this.getType());
		}

	}

	public static abstract class Inventory extends ConduitConnection{

		public static final Comparator<Inventory> ACCEPT_PRIORITY_COMPARATOR =
				(x, y) -> Integer.compare(x.getSettings().getAcceptPriority(), y.getSettings().getAcceptPriority());

				public static final Comparator<Inventory> PROVIDE_PRIORITY_COMPARATOR =
						(x, y) -> Integer.compare(x.getSettings().getProvidePriority(), y.getSettings().getProvidePriority());

						protected ConduitBlockEntity owner;
						protected final Direction fromDir;

						//TODO save these in the codecs
						@Getter
						private final InventoryConnectionSettings settings;

						protected Inventory(final ConduitType type, final Direction fromConduit, final InventoryConnectionSettings settings) {
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
							return new DummyInventoryConnection(this.getType());
						}

						protected void onValueChange(final InventoryConnectionSettings.Setting setting) {
							this.owner.settingsChange();
						}

						public Inventory setOwner(final ConduitBlockEntity owner) {
							if(this.owner != null)
								throw new IllegalStateException("Connection already has owner!");
							this.owner = owner;
							return this;
						}

	}

	private static class DummyInventoryConnection extends Inventory{

		protected DummyInventoryConnection(final ConduitType type) {
			super(type, null, new InventoryConnectionSettings());
		}

		@Override
		public ConduitConnection copyForMap() {
			return new DummyInventoryConnection(this.getType());
		}

		@Override
		public InventoryConnectionSettings getSettings() {
			throw new UnsupportedOperationException();
		}

		@Override
		protected void onValueChange(final InventoryConnectionSettings.Setting setting) {
			throw new UnsupportedOperationException();
		}

	}

	@FunctionalInterface
	public interface ConduitConnectionFactory{

		ConduitConnection apply(ConduitType type, final Direction fromConduit,
				@Nullable ConduitBlockEntity owner);
	}

	private static record Identifier(ConduitConnectionType connectionType, ConduitType type) {

		private static Codec<Identifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				ConduitConnectionType.CODEC.fieldOf("connectionType").forGetter(Identifier::connectionType),
				ModConduits.TYPES_CODEC.fieldOf("conduitType").forGetter(Identifier::type))
				.apply(instance, Identifier::new));

	}

}
