package me.superckl.conduits.conduit;

import lombok.RequiredArgsConstructor;
import me.superckl.conduits.common.block.ConduitBlockEntity;
import me.superckl.conduits.conduit.connection.ConduitConnection;
import me.superckl.conduits.conduit.connection.ConduitConnectionType;
import me.superckl.conduits.conduit.network.inventory.CapabilityInventory;
import net.minecraft.core.Direction;
import net.minecraft.nbt.StringTag;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

@RequiredArgsConstructor
public enum ConduitType implements StringRepresentable{

	ITEM("item", ConnectionHelper.ITEM),
	ENERGY("energy", ConnectionHelper.ENERGY),
	FLUID("fluid", ConnectionHelper.FLUID);

	private final String name;
	private final ConnectionHelper connectionHelper;

	@Override
	public String getSerializedName() {
		return this.name;
	}

	public StringTag tag() {
		return StringTag.valueOf(this.name);
	}

	public boolean canConnect(final Direction dir, final BlockEntity be) {
		return this.connectionHelper.canConnect(dir, be);
	}

	/**
	 * This should only be called through {@link ConduitConnectionType#apply}
	 */
	@Deprecated
	public ConduitConnection.Inventory establishConnection(final Direction dir, final ConduitBlockEntity owner){
		return this.connectionHelper.establishConnection(dir, owner);
	}

	//INTERNAL HELPER CLASSES
	private interface ConnectionHelper {

		Item ITEM = new Item();
		Energy ENERGY = new Energy();
		Fluid FLUID = new Fluid();

		boolean canConnect(Direction dir, BlockEntity be);
		ConduitConnection.Inventory establishConnection(Direction dir, ConduitBlockEntity owner);
	}

	@RequiredArgsConstructor
	private abstract static class CapabilityConnectionHelper<T> implements ConnectionHelper{

		protected final Capability<T> cap;

		@Override
		public boolean canConnect(final Direction dir, final BlockEntity be) {
			return be.getCapability(this.cap, dir.getOpposite()).isPresent();
		}

		@Override
		public abstract CapabilityInventory<T> establishConnection(final Direction dir, ConduitBlockEntity owner);

	}

	private static class Item extends CapabilityConnectionHelper<IItemHandler>{

		private Item() {
			super(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
		}

		@Override
		public CapabilityInventory<IItemHandler> establishConnection(final Direction dir, final ConduitBlockEntity owner) {
			return new CapabilityInventory.Item(owner, dir);
		}

	}

	private static class Energy extends CapabilityConnectionHelper<IEnergyStorage>{

		private Energy() {
			super(CapabilityEnergy.ENERGY);
		}

		@Override
		public CapabilityInventory<IEnergyStorage> establishConnection(final Direction dir, final ConduitBlockEntity owner) {
			return new CapabilityInventory.Energy(owner, dir);
		}

	}

	private static class Fluid extends CapabilityConnectionHelper<IFluidHandler>{

		private Fluid() {
			super(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
		}

		@Override
		public CapabilityInventory<IFluidHandler> establishConnection(final Direction dir, final ConduitBlockEntity owner) {
			return new CapabilityInventory.Fluid(owner, dir);
		}

	}

}
