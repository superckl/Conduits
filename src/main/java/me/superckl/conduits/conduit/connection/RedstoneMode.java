package me.superckl.conduits.conduit.connection;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.superckl.conduits.client.screen.ButtonImageProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

@Getter
@RequiredArgsConstructor
public enum RedstoneMode implements ButtonImageProvider, StringRepresentable{

	IGNORED("ignored", 0, 0),
	ACTIVE_SIGNAL("active_signal", 0, 28),
	INACTIVE_SIGNAL("inactive_signal", 0, 56),
	DISABLED("disabled", 0, 84);

	private static final Map<String, RedstoneMode> BY_NAME = Arrays.stream(RedstoneMode.values()).collect(Collectors.toMap(StringRepresentable::getSerializedName, v -> v));
	public static final Codec<RedstoneMode> CODEC = StringRepresentable.fromEnum(RedstoneMode::values);

	private final String name;
	private final int texX;
	private final int texY;
	private List<Component> tooltip;

	@Override
	public String getSerializedName() {
		return this.name;
	}

	@Override
	public List<Component> getTooltip(){
		if(this.tooltip == null)
			this.tooltip = ImmutableList.of(Component.translatable("conduits.redstone").withStyle(ChatFormatting.WHITE),
					Component.translatable("conduits.redstone."+this.name).withStyle(ChatFormatting.GRAY));
		return this.tooltip;
	}

}