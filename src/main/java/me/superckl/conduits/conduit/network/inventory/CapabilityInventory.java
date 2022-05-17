package me.superckl.conduits.conduit.network.inventory;

import me.superckl.conduits.conduit.network.ConduitNetwork;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

public abstract class CapabilityInventory<T> extends InventoryConnection{

	private final LazyOptional<T> inventory;

	protected boolean accepting = true;
	protected boolean providing = false;

	public CapabilityInventory(final ConduitNetwork owner, final BlockPos conduitPos, final LazyOptional<T> inventory) {
		super(owner, conduitPos);
		if(!inventory.isPresent())
			throw new IllegalArgumentException("Cannot make connection with empty optional!");
		this.inventory = inventory;
		this.inventory.addListener(x -> this.invalidate());
	}

	@Override
	public boolean isAccepting() {
		return this.accepting;
	}

	@Override
	public void setAccepting(final boolean accepting) {
		this.accepting = accepting;
	}

	@Override
	public boolean isProviding() {
		return this.providing;
	}

	@Override
	public void setProviding(final boolean providing) {
		this.providing = providing;
	}

	public static class Item extends CapabilityInventory<IItemHandler>{

		public Item(final ConduitNetwork owner, final BlockPos conduitPos, final LazyOptional<IItemHandler> inventory) {
			super(owner, conduitPos, inventory);
		}

	}

	public static class Energy extends CapabilityInventory<IEnergyStorage>{

		public Energy(final ConduitNetwork owner, final BlockPos conduitPos, final LazyOptional<IEnergyStorage> inventory) {
			super(owner, conduitPos, inventory);
		}

	}

	public static class Fluid extends CapabilityInventory<IFluidHandler>{

		public Fluid(final ConduitNetwork owner, final BlockPos conduitPos, final LazyOptional<IFluidHandler> inventory) {
			super(owner, conduitPos, inventory);
		}

	}

}
