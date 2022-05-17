package me.superckl.conduits.conduit.network.inventory;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.superckl.conduits.conduit.network.ConduitNetwork;
import net.minecraft.core.BlockPos;

@RequiredArgsConstructor
@Getter
public abstract class InventoryConnection {

	private final ConduitNetwork owner;
	private final BlockPos connectedConduit;

	public void invalidate() {
		this.owner.inventoryInvalidated(this);
	}

	public abstract boolean isProviding();
	public abstract boolean isAccepting();
	public abstract void setProviding(boolean providing);
	public abstract void setAccepting(boolean accepting);

}
