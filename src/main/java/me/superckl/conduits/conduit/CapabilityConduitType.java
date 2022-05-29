package me.superckl.conduits.conduit;

import com.mojang.serialization.Codec;

import lombok.RequiredArgsConstructor;
import me.superckl.conduits.common.block.ConduitBlockEntity;
import me.superckl.conduits.conduit.connection.ConduitConnection.Inventory;
import me.superckl.conduits.conduit.connection.InventoryConnectionSettings;
import me.superckl.conduits.conduit.network.inventory.CapabilityInventory;
import me.superckl.conduits.conduit.network.inventory.TransferrableQuantity;
import me.superckl.conduits.conduit.network.inventory.TransferrableQuantity.EnergyQuantity;
import me.superckl.conduits.conduit.network.inventory.TransferrableQuantity.ItemQuantity;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

@RequiredArgsConstructor
public abstract class CapabilityConduitType<T, V extends TransferrableQuantity> extends ConduitType<V>{

	protected final Capability<T> cap;

	@Override
	public boolean canConnect(final Direction dir, final BlockEntity be) {
		return be.getCapability(this.cap, dir.getOpposite()).isPresent();
	}

	public static class Item extends CapabilityConduitType<IItemHandler, TransferrableQuantity.ItemQuantity>{

		public Item() {
			super(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
		}

		@Override
		protected Inventory<ItemQuantity> establishConnection(final Direction dir, final ConduitBlockEntity owner) {
			return new CapabilityInventory.Item(dir, new InventoryConnectionSettings()).setOwner(owner);
		}

		@Override
		protected Codec<CapabilityInventory.Item> inventoryCodec() {
			return CapabilityInventory.Item.CODEC;
		}

	}

	public static class Energy extends CapabilityConduitType<IEnergyStorage, TransferrableQuantity.EnergyQuantity>{

		public Energy() {
			super(CapabilityEnergy.ENERGY);
		}

		@Override
		protected Inventory<EnergyQuantity> establishConnection(final Direction dir, final ConduitBlockEntity owner) {
			return new CapabilityInventory.Energy(dir, new InventoryConnectionSettings()).setOwner(owner);
		}

		@Override
		protected Codec<CapabilityInventory.Energy> inventoryCodec() {
			return CapabilityInventory.Energy.CODEC;
		}

	}

	public static class Fluid extends CapabilityConduitType<IFluidHandler, TransferrableQuantity.FluidQuantity>{

		public Fluid() {
			super(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
		}

		@Override
		protected Inventory<TransferrableQuantity.FluidQuantity> establishConnection(final Direction dir, final ConduitBlockEntity owner) {
			return new CapabilityInventory.Fluid(dir, new InventoryConnectionSettings()).setOwner(owner);
		}

		@Override
		protected Codec<CapabilityInventory.Fluid> inventoryCodec() {
			return CapabilityInventory.Fluid.CODEC;
		}

	}

}
