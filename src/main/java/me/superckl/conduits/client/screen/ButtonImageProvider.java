package me.superckl.conduits.client.screen;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.chat.Component;

public interface ButtonImageProvider {

	int getTexX();
	int getTexY();
	List<Component> getTooltip();

	default int getWidth() { return 28;}
	default int getHeight() { return 28;}

	@RequiredArgsConstructor
	@Getter
	public static class Static implements ButtonImageProvider{

		private final int texX;
		private final int texY;
		private final List<Component> tooltip;
		private final int width;
		private final int height;

	}

}
