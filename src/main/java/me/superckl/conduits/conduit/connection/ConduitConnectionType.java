package me.superckl.conduits.conduit.connection;

import javax.annotation.Nullable;

import lombok.RequiredArgsConstructor;
import me.superckl.conduits.common.block.ConduitBlockEntity;
import me.superckl.conduits.conduit.ConduitType;
import me.superckl.conduits.conduit.connection.ConduitConnection.ConduitConnectionFactory;
import net.minecraft.core.Direction;
import net.minecraft.nbt.StringTag;
import net.minecraft.util.StringRepresentable;

@RequiredArgsConstructor
public enum ConduitConnectionType implements StringRepresentable, ConduitConnectionFactory{

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

	@SuppressWarnings("deprecation")
	@Override
	public ConduitConnection apply(final ConduitType type, final Direction fromConduit, @Nullable final ConduitBlockEntity owner) {
		return switch(this) {
		case CONDUIT -> new ConduitConnection.Conduit(type);
		case INVENTORY -> type.establishConnection(fromConduit, owner);
		default -> null;
		};
	}

}