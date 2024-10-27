package me.superckl.conduits.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;

public abstract class DecalButton extends Button {

    protected final Screen owner;
    protected int decalX = 0;
    protected int decalY = 0;

    public DecalButton(final Screen owner, final int pX, final int pY, final int pWidth, final int pHeight, final OnPress pOnPress) {
        super(pX, pY, pWidth, pHeight, Component.empty(), pOnPress, Button.DEFAULT_NARRATION);
        this.owner = owner;
    }

    @Override
    public void renderWidget(GuiGraphics pGuiGraphics, final int mouseX, final int mouseY, final float partialTick) {
        pGuiGraphics.blitSprite(SPRITES.get(this.active, this.isHoveredOrFocused()), this.getX(), this.getY(), this.width, this.height);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, InventoryConnectionScreen.WIDGETS_LOCATION);
        final ButtonImageProvider provider = this.getProvider();
        final int decalWidth = this.width - 2 * this.decalX;
        final int decalHeight = this.height - 2 * this.decalY;
        pGuiGraphics.blit(InventoryConnectionScreen.WIDGETS_LOCATION, this.getX() + this.decalX, this.getY() + this.decalY, decalWidth, decalHeight, provider.getTexX(),
                provider.getTexY(), provider.getWidth(), provider.getHeight(), 256, 256);
    }

    public abstract ButtonImageProvider getProvider();

    public static class Static extends DecalButton {

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
