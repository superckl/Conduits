package me.superckl.conduits.conduit;

import com.mojang.serialization.Codec;

import lombok.RequiredArgsConstructor;
import me.superckl.conduits.common.block.ConduitBlockEntity;
import me.superckl.conduits.conduit.connection.ConduitConnection.Inventory;
import me.superckl.conduits.conduit.connection.InventoryConnectionSettings;
import me.superckl.conduits.conduit.connection.RedstoneMode;
import me.superckl.conduits.conduit.network.inventory.CapabilityInventory;
import me.superckl.conduits.conduit.network.inventory.TransferrableQuantity;
import me.superckl.conduits.conduit.network.inventory.TransferrableQuantity.EnergyQuantity;
import me.superckl.conduits.conduit.network.inventory.TransferrableQuantity.ItemQuantity;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.Comparator;

@RequiredArgsConstructor
public abstract class CapabilityConduitType<T, V extends TransferrableQuantity> extends ConduitType<V> {

	protected final BlockCapability<T, Direction> cap;

	@Override
	public boolean canConnect(final Direction dir, final BlockEntity be) {
		return be.getLevel().getCapability(this.cap, be.getBlockPos(), be.getBlockState(), be, dir.getOpposite()) != null;
	}

	public static class Item extends CapabilityConduitType<IItemHandler, TransferrableQuantity.ItemQuantity>{

		public Item() {
			super(Capabilities.ItemHandler.BLOCK);
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
			super(Capabilities.EnergyStorage.BLOCK);
		}

		@Override
		protected Inventory<EnergyQuantity> establishConnection(final Direction dir, final ConduitBlockEntity owner) {
			return new CapabilityInventory.Energy(dir, new InventoryConnectionSettings().setProvideRedstoneMode(RedstoneMode.IGNORED)).setOwner(owner);
		}

		@Override
		protected Codec<CapabilityInventory.Energy> inventoryCodec() {
			return CapabilityInventory.Energy.CODEC;
		}

	}

	public static class Fluid extends CapabilityConduitType<IFluidHandler, TransferrableQuantity.FluidQuantity>{

		public Fluid() {
			super(Capabilities.FluidHandler.BLOCK);
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
