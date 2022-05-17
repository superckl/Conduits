package me.superckl.conduits.conduit.network.inventory;

import me.superckl.conduits.ModContainers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class InventoryConnectionMenu extends AbstractContainerMenu{

	public InventoryConnectionMenu(final int pContainerId, final Inventory inv) {

		super(ModContainers.INVENTORY_CONNECTION.get(), pContainerId);
	}

	@Override
	public boolean stillValid(final Player pPlayer) {
		return true;
	}

}
