package me.superckl.conduits.conduit.connection;

import lombok.RequiredArgsConstructor;
import net.minecraft.nbt.StringTag;
import net.minecraft.util.StringRepresentable;

@RequiredArgsConstructor
public enum ConduitConnectionType implements StringRepresentable{

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