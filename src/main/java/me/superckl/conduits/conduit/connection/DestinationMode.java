package me.superckl.conduits.conduit.connection;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.mojang.serialization.Codec;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.superckl.conduits.client.screen.ButtonImageProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.StringRepresentable;

@RequiredArgsConstructor
@Getter
public enum DestinationMode implements ButtonImageProvider, StringRepresentable{

	NEAREST("nearest", 0, 0, new TranslatableComponent("conduits.destination.nearest")),
	RANDOM("random", 0, 0, new TranslatableComponent("conduits.destination.random")),
	ROUND_ROBIN("round_robin", 0, 0, new TranslatableComponent("conduits.destination.round_robin"));

	private static final Map<String, DestinationMode> BY_NAME = Arrays.stream(DestinationMode.values()).collect(Collectors.toMap(StringRepresentable::getSerializedName, v -> v));
	public static final Codec<DestinationMode> CODEC = StringRepresentable.fromEnum(DestinationMode::values, DestinationMode.BY_NAME::get);

	private final String name;
	private final int texX;
	private final int texY;
	private final Component tooltip;

	@Override
	public String getSerializedName() {
		return this.name;
	}

}
