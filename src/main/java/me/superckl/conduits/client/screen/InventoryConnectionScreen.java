package me.superckl.conduits.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;

import me.superckl.conduits.conduit.network.inventory.InventoryConnectionMenu;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class InventoryConnectionScreen extends AbstractContainerScreen<InventoryConnectionMenu>{

	public InventoryConnectionScreen(final InventoryConnectionMenu pMenu, final Inventory pPlayerInventory, final Component pTitle) {
		super(pMenu, pPlayerInventory, pTitle);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void renderBg(final PoseStack pPoseStack, final float pPartialTick, final int pMouseX, final int pMouseY) {
		// TODO Auto-generated method stub

	}

}
