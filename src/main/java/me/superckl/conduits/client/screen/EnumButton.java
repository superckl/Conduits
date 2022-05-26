package me.superckl.conduits.client.screen;

import me.superckl.conduits.util.EnumCycler;
import net.minecraft.client.gui.screens.Screen;

public class EnumButton<T extends Enum<T> & ButtonImageProvider> extends DecalButton{

	private final EnumCycler<T> cycler;
	private final ValueChangeHandler<T> handler;

	public EnumButton(final Screen owner, final Class<T> enumClass, final T initial, final int xPos, final int yPos, final int width, final int height,
			final ValueChangeHandler<T> handler) {
		super(owner, xPos, yPos, width, height, x -> {});
		this.handler = handler;
		this.decalX = 2;
		this.decalY = 2;
		this.cycler = new EnumCycler<>(enumClass, initial);
	}

	@Override
	public void onPress() {
		this.cycler.cycle();
		this.handler.onValueChange(this.cycler.getValue());
	}

	public void setValue(final T value) {
		this.cycler.setValue(value);
	}

	@Override
	public ButtonImageProvider getProvider() {
		return this.cycler.getValue();
	}

	public interface ValueChangeHandler<T>{

		void onValueChange(T newValue);

	}

}
