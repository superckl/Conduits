package me.superckl.conduits.conduit;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.mojang.serialization.Codec;

import lombok.RequiredArgsConstructor;
import net.minecraft.nbt.StringTag;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public enum ConduitTier implements StringRepresentable{

	EARLY("early"),
	MIDDLE("middle"),
	LATE("late");

	private static final Map<String, ConduitTier> BY_NAME = Arrays.stream(ConduitTier.values()).collect(Collectors.toMap(StringRepresentable::getSerializedName, v -> v));
	public static final Codec<ConduitTier> CODEC = StringRepresentable.fromEnum(ConduitTier::values);

	private final String name;

	@Override
	public @NotNull String getSerializedName() {
		return this.name;
	}

	public StringTag tag() {
		return StringTag.valueOf(this.name);
	}

}
