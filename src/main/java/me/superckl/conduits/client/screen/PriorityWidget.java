package me.superckl.conduits.client.screen;

import java.util.function.IntConsumer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import lombok.Setter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;

public class PriorityWidget extends AbstractWidget{

	private final Font font;
	private final IntConsumer onChange;
	@Setter
	private int value;

	private final DecalButton decreaseButton;
	private final DecalButton increaseButton;

	private final TranslatableComponent priorityText = new TranslatableComponent("conduits.gui.connection.priority");

	@SuppressWarnings("resource")
	public PriorityWidget(final Screen owner, final int initial, final int pX, final int pY, final IntConsumer onValueChange) {
		super(pX, pY, 44, 19, TextComponent.EMPTY);
		this.font = owner.getMinecraft().font;
		this.onChange = onValueChange;
		this.value = initial;
		this.decreaseButton = new DecalButton.Static(owner, this.x+4, this.y+this.font.lineHeight+1, 8, 8,
				new ButtonImageProvider.Static(0, 112, TextComponent.EMPTY, 28, 28), x -> {this.changeValue(-1);});
		this.increaseButton = new DecalButton.Static(owner, this.x+this.width-12, this.y+this.font.lineHeight+1, 8, 8,
				new ButtonImageProvider.Static(0, 140, TextComponent.EMPTY, 28, 28), x -> {this.changeValue(1);});
		this.decreaseButton.active = false;
	}

	@Override
	public void render(final PoseStack pPoseStack, final int pMouseX, final int pMouseY, final float pPartialTick) {
		if(!this.visible)
			return;
		this.decreaseButton.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
		this.increaseButton.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		GuiComponent.drawCenteredString(pPoseStack, this.font, String.valueOf(this.value), this.x + this.width/2,
				this.y+this.height-this.font.lineHeight, this.getFGColor());
		final int textWidth = this.font.width(this.priorityText);
		this.font.draw(pPoseStack, this.priorityText, this.x+this.width/2-textWidth/2+1, this.y, 4210752);

	}

	@Override
	public boolean clicked(final double pMouseX, final double pMouseY) {
		if(this.decreaseButton.isMouseOver(pMouseX, pMouseY) && this.decreaseButton.isActive()) {
			this.decreaseButton.onClick(pMouseX, pMouseY);
			return true;
		}
		if(this.increaseButton.isMouseOver(pMouseX, pMouseY) && this.increaseButton.isActive()) {
			this.increaseButton.onClick(pMouseX, pMouseY);
			return true;
		}
		return false;
	}

	public void changeValue(final int change) {
		final int newValue = Mth.clamp(this.value+change, 0, 99);
		if(newValue != this.value) {
			this.value = newValue;
			this.onChange.accept(this.value);
		}
		this.increaseButton.active = this.value != 99;
		this.decreaseButton.active = this.value != 0;
	}

	@Override
	public void updateNarration(final NarrationElementOutput pNarrationElementOutput) {
		// TODO
	}

}
