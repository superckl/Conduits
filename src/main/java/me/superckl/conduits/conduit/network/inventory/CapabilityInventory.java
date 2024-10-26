package me.superckl.conduits.conduit.network.inventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import lombok.Getter;
import me.superckl.conduits.ModConduits;
import me.superckl.conduits.conduit.ConduitType;
import me.superckl.conduits.conduit.connection.ConduitConnection;
import me.superckl.conduits.conduit.connection.InventoryConnectionSettings;
import me.superckl.conduits.conduit.network.inventory.TransferrableQuantity.CapabilityQuantity;
import me.superckl.conduits.conduit.network.inventory.TransferrableQuantity.EnergyQuantity;
import me.superckl.conduits.conduit.network.inventory.TransferrableQuantity.FluidQuantity;
import me.superckl.conduits.conduit.network.inventory.TransferrableQuantity.ItemQuantity;
import me.superckl.conduits.util.FluidHandlerUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

public abstract class CapabilityInventory<T, V extends CapabilityQuantity<T>> extends ConduitConnection.Inventory<V>{

	private final BlockCapability<T, Direction> cap;
	protected T inventory;
	@Getter
    private boolean resolved = false;

	public CapabilityInventory(final ConduitType<V> type, final Direction fromDir, final InventoryConnectionSettings settings, final BlockCapability<T, Direction> cap) {
		super(type, fromDir, settings);
		this.cap = cap;
	}

	@Override
	public boolean resolve(){
		this.inventory = this.owner.getLevel().getCapability(this.cap, this.owner.getBlockPos().relative(this.fromDir), this.fromDir);
		this.resolved = true;
        return this.inventory != null;
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
	public void accept(final V quantity) {
		if(this.inventory != null)
			quantity.insertInto(this.inventory);
	}

	public static class Item extends CapabilityInventory<IItemHandler, TransferrableQuantity.ItemQuantity>{

		public static Codec<Item> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Direction.CODEC.fieldOf("fromDir").forGetter(x -> x.fromDir),
				InventoryConnectionSettings.CODEC.fieldOf("settings").forGetter(Inventory::getSettings))
				.apply(instance, Item::new));

		public Item(final Direction fromDir, final InventoryConnectionSettings settings) {
			super(ModConduits.ITEM.get(), fromDir, settings, Capabilities.ItemHandler.BLOCK);

		}

		@Override
		public List<ItemQuantity> nextAvailable() {
			if(this.inventory == null)
				return Collections.emptyList();
			else {
				final int slots = this.inventory.getSlots();
				if (slots == 0)
					return Collections.<ItemQuantity>emptyList();
				final List<ItemQuantity> items = new ArrayList<>(slots);
				for (int i = 0; i < slots; i++) {
					final ItemStack stack = this.inventory.getStackInSlot(i);
					if (!stack.isEmpty())
						items.add(new ItemQuantity(this.inventory, i));
				}
				return items;
			}
		}

		@Override
		public void setupReceivingCache() {
			// TODO Auto-generated method stub
			super.setupReceivingCache();
		}

	}

	public static class Energy extends CapabilityInventory<IEnergyStorage, TransferrableQuantity.EnergyQuantity>{

		public static Codec<Energy> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Direction.CODEC.fieldOf("fromDir").forGetter(x -> x.fromDir),
				InventoryConnectionSettings.CODEC.fieldOf("settings").forGetter(Inventory::getSettings))
				.apply(instance, Energy::new));

		public Energy(final Direction fromDir, final InventoryConnectionSettings settings) {
			super(ModConduits.ENERGY.get(), fromDir, settings, Capabilities.EnergyStorage.BLOCK);
		}

		@Override
		public List<EnergyQuantity> nextAvailable() {
			if(this.inventory == null || this.inventory.getEnergyStored() <= 0)
				return ImmutableList.of();
			else
				return ImmutableList.of(new EnergyQuantity(this.inventory));
		}

	}

	public static class Fluid extends CapabilityInventory<IFluidHandler, TransferrableQuantity.FluidQuantity>{

		public static Codec<Fluid> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Direction.CODEC.fieldOf("fromDir").forGetter(x -> x.fromDir),
				InventoryConnectionSettings.CODEC.fieldOf("settings").forGetter(Inventory::getSettings))
				.apply(instance, Fluid::new));

		public Fluid(final Direction fromDir, final InventoryConnectionSettings settings) {
			super(ModConduits.FLUID.get(), fromDir, settings, Capabilities.FluidHandler.BLOCK);
		}

		@Override
		public List<FluidQuantity> nextAvailable() {
			if(this.inventory == null || !FluidHandlerUtil.isEmpty((this.inventory)))
				return ImmutableList.of();
			else
				return ImmutableList.of(new FluidQuantity(this.inventory));
		}

	}

}
