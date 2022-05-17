package me.superckl.conduits.conduit.connection;

import lombok.RequiredArgsConstructor;
import me.superckl.conduits.conduit.network.ConduitNetwork;
import me.superckl.conduits.conduit.network.inventory.CapabilityInventory;
import me.superckl.conduits.conduit.network.inventory.InventoryConnection;
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

public interface ConnectionHelper {

	Item ITEM = new Item();
	Energy ENERGY = new Energy();
	Fluid FLUID = new Fluid();

	boolean canConnect(Direction dir, BlockEntity be);
	InventoryConnection establishConnection(Direction dir, BlockEntity be, ConduitNetwork owner);

	@RequiredArgsConstructor
	public abstract static class CapabilityConnectionHelper<T> implements ConnectionHelper{

		protected final Capability<T> cap;

		@Override
		public boolean canConnect(final Direction dir, final BlockEntity be) {
			return be.getCapability(this.cap, dir).isPresent();
		}

		protected LazyOptional<T> get(final Direction dir, final BlockEntity be){
			return be.getCapability(this.cap, dir);
		}

		@Override
		public abstract CapabilityInventory<T> establishConnection(final Direction dir, final BlockEntity be, ConduitNetwork owner);

	}

	public static class Item extends CapabilityConnectionHelper<IItemHandler>{

		private Item() {
			super(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
		}

		@Override
		public CapabilityInventory<IItemHandler> establishConnection(final Direction dir, final BlockEntity be, final ConduitNetwork owner) {
			return new CapabilityInventory.Item(owner, be.getBlockPos().relative(dir), this.get(dir, be));
		}

	}

	public static class Energy extends CapabilityConnectionHelper<IEnergyStorage>{

		private Energy() {
			super(CapabilityEnergy.ENERGY);
		}

		@Override
		public CapabilityInventory<IEnergyStorage> establishConnection(final Direction dir, final BlockEntity be, final ConduitNetwork owner) {
			return new CapabilityInventory.Energy(owner, be.getBlockPos().relative(dir), this.get(dir, be));
		}

	}

	public static class Fluid extends CapabilityConnectionHelper<IFluidHandler>{

		private Fluid() {
			super(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
		}

		@Override
		public CapabilityInventory<IFluidHandler> establishConnection(final Direction dir, final BlockEntity be, final ConduitNetwork owner) {
			return new CapabilityInventory.Fluid(owner, be.getBlockPos().relative(dir), this.get(dir, be));
		}

	}
}
