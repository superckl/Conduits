package me.superckl.conduits.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.client.gui.GuiUtils;

public abstract class DecalButton extends Button{

	protected final Screen owner;
	protected int decalX = 0;
	protected int decalY = 0;

	public DecalButton(final Screen owner, final int pX, final int pY, final int pWidth, final int pHeight, final OnPress pOnPress) {
		super(pX, pY, pWidth, pHeight, TextComponent.EMPTY, pOnPress);
		this.owner = owner;
	}

	@Override
	public void renderButton(final PoseStack poseStack, final int mouseX, final int mouseY, final float partialTick) {
		final int k = this.getYImage(this.isHovered);
		GuiUtils.drawContinuousTexturedBox(poseStack, AbstractWidget.WIDGETS_LOCATION, this.x, this.y, 0, 46 + k * 20,
				this.width, this.height, 200, 20, 2, 3, 2, 2, this.getBlitOffset());

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, InventoryConnectionScreen.WIDGETS_LOCATION);
		final ButtonImageProvider provider = this.getProvider();
		final int decalWidth = this.width-2*this.decalX;
		final int decalHeight = this.height-2*this.decalY;
		GuiComponent.blit(poseStack, this.x+this.decalX, this.y+this.decalY, decalWidth, decalHeight, provider.getTexX(),
				provider.getTexY(), provider.getWidth(), provider.getHeight(), 256, 256);
	}

	@Override
	public void renderToolTip(final PoseStack pPoseStack, final int pMouseX, final int pMouseY) {
		this.owner.renderComponentTooltip(pPoseStack, this.getProvider().getTooltip(), pMouseX, pMouseY);
	}

	public abstract ButtonImageProvider getProvider();

	public static class Static extends DecalButton{

		private final ButtonImageProvider provider;

		public Static(final Screen owner, final int pX, final int pY, final int pWidth, final int pHeight, final ButtonImageProvider provider, final OnPress pOnPress) {
			super(owner, pX, pY, pWidth, pHeight, pOnPress);
			this.provider = provider;
		}

		@Override
		public ButtonImageProvider getProvider() {
			return this.provider;
		}

	}

}
