package me.superckl.conduits.conduit.connection;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.mojang.serialization.Codec;

import lombok.RequiredArgsConstructor;
import net.minecraft.nbt.StringTag;
import net.minecraft.util.StringRepresentable;

@RequiredArgsConstructor
public enum ConduitConnectionType implements StringRepresentable{

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

}