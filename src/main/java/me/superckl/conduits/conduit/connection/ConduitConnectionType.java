package me.superckl.conduits.conduit.connection;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;

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

	private static final Map<String, ConduitConnectionType> BY_NAME = Arrays.stream(ConduitConnectionType.values()).collect(Collectors.toMap(StringRepresentable::getSerializedName, v -> v));
	public static final Codec<ConduitConnectionType> CODEC = StringRepresentable.fromEnum(ConduitConnectionType::values, ConduitConnectionType.BY_NAME::get);

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