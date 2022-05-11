package me.superckl.conduits;

import lombok.RequiredArgsConstructor;
import net.minecraft.nbt.StringTag;
import net.minecraft.util.StringRepresentable;

@RequiredArgsConstructor
public enum ConduitTier implements StringRepresentable{

	EARLY("early"),
	MIDDLE("middle"),
	LATE("late");

	private final String name;

	@Override
	public String getSerializedName() {
		return this.name;
	}

	public StringTag tag() {
		return StringTag.valueOf(this.name);
	}

}
