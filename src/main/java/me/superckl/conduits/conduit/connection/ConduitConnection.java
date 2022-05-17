package me.superckl.conduits.conduit.connection;

import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.common.base.Objects;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.superckl.conduits.common.block.ConduitBlockEntity;
import me.superckl.conduits.conduit.ConduitType;
import me.superckl.conduits.util.NBTUtil;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ConduitConnection {

	@Getter
	private final ConduitType type;

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

	public final CompoundTag tag() {
		final CompoundTag tag = new CompoundTag();
		tag.put("type", this.getConnectionType().tag());
		return tag;
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
	protected void writeAdditional(final CompoundTag tag) {}
	protected void readAdditioanl(final CompoundTag tag) {}

	public static ConduitConnection fromTag(final CompoundTag tag, final Function<ConduitConnectionType, ConduitConnection> parameterizedFactory) {
		final ConduitConnectionType connType = NBTUtil.enumFromString(ConduitConnectionType.class, tag.getString("type"));
		final ConduitConnection connection = parameterizedFactory.apply(connType);
		connection.readAdditioanl(tag);
		return connection;
	}

	public static class Conduit extends ConduitConnection{

		protected Conduit(final ConduitType type) {
			super(type);
		}

		@Override
		public ConduitConnection copyForMap() {
			return new Conduit(this.getType());
		}

	}

	public static abstract class Inventory extends ConduitConnection{

		protected final ConduitBlockEntity owner;
		protected final Direction fromDir;

		protected Inventory(final ConduitType type, final Direction fromConduit, final ConduitBlockEntity owner) {
			super(type);
			this.owner = owner;
			this.fromDir = fromConduit;
		}

		public void invalidate() {
			if(!this.owner.isRemoved())
				this.owner.inventoryInvalidated(this.fromDir, this);
		}

		@Override
		public ConduitConnection copyForMap() {
			return new DummyInventoryConnection(this.getType());
		}

		public abstract boolean isProviding();
		public abstract boolean isAccepting();
		public abstract void setProviding(boolean providing);
		public abstract void setAccepting(boolean accepting);

	}

	private static class DummyInventoryConnection extends Inventory{

		protected DummyInventoryConnection(final ConduitType type) {
			super(type, null, null);
		}

		@Override
		public ConduitConnection copyForMap() {
			return new DummyInventoryConnection(this.getType());
		}

		@Override
		public boolean isProviding() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isAccepting() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setProviding(final boolean providing) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setAccepting(final boolean accepting) {
			throw new UnsupportedOperationException();
		}

	}

	@FunctionalInterface
	public interface ConduitConnectionFactory{

		ConduitConnection apply(ConduitType type, final Direction fromConduit,
				@Nullable ConduitBlockEntity owner);
	}

}
