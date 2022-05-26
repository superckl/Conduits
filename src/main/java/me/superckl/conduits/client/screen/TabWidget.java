package me.superckl.conduits.client.screen;

import java.util.function.Consumer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;

public class TabWidget extends AbstractWidget{

	private final Type type;
	private final Consumer<? super TabWidget> onPress;
	private final ButtonImageProvider imageProvider;
	private final Tooltip<? super TabWidget> tooltip;
	@Setter @Getter
	private boolean selected;

	public TabWidget(final int pX, final int pY, final Type type, final Tooltip<? super TabWidget> tooltip,
			final Consumer<? super TabWidget> onPress, final ButtonImageProvider imageProvider) {
		super(pX, pY, type.width, 30, TextComponent.EMPTY);
		this.type = type;
		this.onPress = onPress;
		this.imageProvider = imageProvider;
		this.tooltip = tooltip;
	}

	@Override
	public void renderButton(final PoseStack pPoseStack, final int pMouseX, final int pMouseY, final float pPartialTick) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, InventoryConnectionScreen.BACKGROUND_LOCATION);
		final int yOffset = this.selected ? -2 : 0;

		final int texXOffset = switch(this.type) {
		case LEFT -> 0;
		case MIDDLE -> Type.LEFT.width;
		case RIGHT -> Type.LEFT.width+Type.MIDDLE.width;
		};
		final int texYOffset = this.selected ? 30 : 0;
		this.blit(pPoseStack, this.x, this.y+yOffset, texXOffset, 166+texYOffset, this.type.width, 30-yOffset);

		RenderSystem.setShaderTexture(0, InventoryConnectionScreen.WIDGETS_LOCATION);
		final int decalWidth = this.width-2*5;
		final int decalHeight = this.height-2*6;
		GuiComponent.blit(pPoseStack, this.x+5, this.y+7, decalWidth, decalHeight, this.imageProvider.getTexX(),
				this.imageProvider.getTexY(), this.imageProvider.getWidth(), this.imageProvider.getHeight(), 256, 256);
	}

	@Override
	public void renderToolTip(final PoseStack pPoseStack, final int pMouseX, final int pMouseY) {
		this.tooltip.renderTooltip(this, pPoseStack, pMouseX, pMouseY);
	}

	@Override
	protected boolean clicked(final double pMouseX, final double pMouseY) {
		if(this.isHoveredOrFocused()) {
			final boolean selected = this.selected;
			this.onPress.accept(this);
			return selected != this.selected;
		}
		return false;
	}

	@Override
	public void updateNarration(final NarrationElementOutput pNarrationElementOutput) {
		// TODO
	}

	@RequiredArgsConstructor
	@Getter
	public enum Type{
		LEFT(27),
		MIDDLE(28),
		RIGHT(28);

		private final int width;
	}

}
