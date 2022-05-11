package me.superckl.conduits;

import javax.annotation.Nullable;

import lombok.RequiredArgsConstructor;
import net.minecraft.util.StringRepresentable;

@RequiredArgsConstructor
public enum PartType implements StringRepresentable{
	JOINT("joint", true),
	SEGMENT("segment", true),
	CONNECTION("inventory_connection", false),
	MIXED_JOINT("mixed_joint", false);

	private final String name;
	private final boolean typed;

	@Override
	public String getSerializedName() {
		return this.name;
	}

	public String path(@Nullable final ConduitType type) {
		final StringBuilder builder = new StringBuilder("conduit/");
		if(this.typed && type != null)
			builder.append(type.getSerializedName()).append('_');
		return builder.append(this.getSerializedName()).toString();
	}
}