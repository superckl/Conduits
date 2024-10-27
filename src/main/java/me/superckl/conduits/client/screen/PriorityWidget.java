package me.superckl.conduits.client.screen;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Setter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.function.IntConsumer;

public class PriorityWidget extends AbstractWidget {

    private final Font font;
    private final IntConsumer onChange;
    @Setter
    private int value;

    private final DecalButton decreaseButton;
    private final DecalButton increaseButton;

    private final Component priorityText = Component.translatable("conduits.gui.connection.priority");

    public PriorityWidget(final Screen owner, final int initial, final int pX, final int pY, final IntConsumer onValueChange) {
        super(pX, pY, 44, 19, Component.empty());
        this.font = owner.getMinecraft().font;
        this.onChange = onValueChange;
        this.value = initial;
        this.decreaseButton = new DecalButton.Static(owner, this.getX() + 4, this.getY() + this.font.lineHeight + 1, 8, 8,
                new ButtonImageProvider.Static(0, 112, ImmutableList.of(Component.empty()), 28, 28), x -> {
            this.changeValue(-1);
        });
        this.increaseButton = new DecalButton.Static(owner, this.getX() + this.width - 12, this.getY() + this.font.lineHeight + 1, 8, 8,
                new ButtonImageProvider.Static(0, 140, ImmutableList.of(Component.empty()), 28, 28), x -> {
            this.changeValue(1);
        });
        this.decreaseButton.active = false;
    }

    @Override
    public void renderWidget(final GuiGraphics pGuiGraphics, final int pMouseX, final int pMouseY, final float pPartialTick) {
        if (!this.visible)
            return;
        this.decreaseButton.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.increaseButton.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        pGuiGraphics.drawCenteredString(this.font, String.valueOf(this.value), this.getX() + this.width / 2,
                this.getY() + this.height - this.font.lineHeight, this.getFGColor());
        final int textWidth = this.font.width(this.priorityText);
        pGuiGraphics.drawString(this.font, this.priorityText, this.getX() + this.width / 2 - textWidth / 2 + 1, this.getY(), 4210752);
    }

    @Override
    public boolean clicked(final double pMouseX, final double pMouseY) {
        if (this.decreaseButton.isMouseOver(pMouseX, pMouseY) && this.decreaseButton.isActive()) {
            this.decreaseButton.onClick(pMouseX, pMouseY);
            return true;
        }
        if (this.increaseButton.isMouseOver(pMouseX, pMouseY) && this.increaseButton.isActive()) {
            this.increaseButton.onClick(pMouseX, pMouseY);
            return true;
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
        //TODO
    }

    public void changeValue(final int change) {
        final int newValue = Mth.clamp(this.value + change, 0, 99);
        if (newValue != this.value) {
            this.value = newValue;
            this.onChange.accept(this.value);
        }
        this.increaseButton.active = this.value != 99;
        this.decreaseButton.active = this.value != 0;
    }

}
