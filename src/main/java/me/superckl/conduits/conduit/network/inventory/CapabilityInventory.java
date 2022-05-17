package me.superckl.conduits.conduit.network.inventory;

import com.google.common.base.Preconditions;

import me.superckl.conduits.common.block.ConduitBlockEntity;
import me.superckl.conduits.conduit.ConduitType;
import me.superckl.conduits.conduit.connection.ConduitConnection;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public abstract class CapabilityInventory<T> extends ConduitConnection.Inventory{

	private final Capability<T> cap;
	private LazyOptional<T> inventory;
	private boolean resolved = false;

	protected boolean accepting = true;
	protected boolean providing = false;

	public CapabilityInventory(final ConduitType type, final ConduitBlockEntity owner, final Direction fromDir, final Capability<T> cap) {
		super(type, fromDir, null, owner);
		Preconditions.checkNotNull(owner);
		this.cap = cap;
	}

	@Override
	public boolean resolve(){
		final BlockEntity be = this.owner.getLevel().getBlockEntity(this.owner.getBlockPos().relative(this.fromDir));
		if(be == null) {
			this.inventory = LazyOptional.empty();
			return false;
		}
		this.inventory = be.getCapability(this.cap);
		this.resolved = true;
		if(this.inventory.isPresent()) {
			this.inventory.addListener(x -> this.invalidate());
			return true;
		}
		return false;
	}

	public boolean isResolved() {
		return this.resolved;
	}

	@Override
	public void invalidate() {
		//First attempt to re-establish the connection incase it was just a change in the cap details
		if(this.owner.isRemoved() || this.resolve())
			return;
		//Let the superclass do any notifications that it needs to
		super.invalidate();
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

		public Item(final ConduitBlockEntity owner, final Direction fromDir) {
			super(ConduitType.ITEM, owner, fromDir, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
		}

	}

	public static class Energy extends CapabilityInventory<IEnergyStorage>{

		public Energy(final ConduitBlockEntity owner, final Direction fromDir) {
			super(ConduitType.ENERGY, owner, fromDir, CapabilityEnergy.ENERGY);
		}

	}

	public static class Fluid extends CapabilityInventory<IFluidHandler>{

		public Fluid(final ConduitBlockEntity owner, final Direction fromDir) {
			super(ConduitType.FLUID, owner, fromDir, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
		}

	}

}
