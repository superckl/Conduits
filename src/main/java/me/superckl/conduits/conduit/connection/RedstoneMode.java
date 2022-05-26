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

@Getter
@RequiredArgsConstructor
public enum RedstoneMode implements ButtonImageProvider, StringRepresentable{

	IGNORED("ignored", 0, 0, new TranslatableComponent("conduits.redstone.ignore")),
	ACTIVE_SIGNAL("active_signal", 0, 28, new TranslatableComponent("conduits.redstone.active_signal")),
	INACTIVE_SIGNAL("inactive_signal", 0, 56, new TranslatableComponent("conduits.redstone.inactive_signal")),
	DISABLED("disabled", 0, 84, new TranslatableComponent("conduits.redstone.disabled"));

	private static final Map<String, RedstoneMode> BY_NAME = Arrays.stream(RedstoneMode.values()).collect(Collectors.toMap(StringRepresentable::getSerializedName, v -> v));
	public static final Codec<RedstoneMode> CODEC = StringRepresentable.fromEnum(RedstoneMode::values, RedstoneMode.BY_NAME::get);

	private final String name;
	private final int texX;
	private final int texY;
	private final Component tooltip;

	@Override
	public String getSerializedName() {
		return this.name;
	}

}