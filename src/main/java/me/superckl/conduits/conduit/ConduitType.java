package me.superckl.conduits.conduit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.superckl.conduits.conduit.connection.ConnectionHelper;
import net.minecraft.nbt.StringTag;
import net.minecraft.util.StringRepresentable;

@RequiredArgsConstructor
public enum ConduitType implements StringRepresentable{

	ITEM("item", ConnectionHelper.ITEM),
	ENERGY("energy", ConnectionHelper.ENERGY),
	FLUID("fluid", ConnectionHelper.FLUID);

	private final String name;
	@Getter
	private final ConnectionHelper connectionHelper;

	@Override
	public String getSerializedName() {
		return this.name;
	}

	public StringTag tag() {
		return StringTag.valueOf(this.name);
	}

}
