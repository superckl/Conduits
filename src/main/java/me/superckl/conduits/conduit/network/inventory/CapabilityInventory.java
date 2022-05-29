package me.superckl.conduits.conduit.network.inventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public abstract class CapabilityInventory<T, V extends CapabilityQuantity<T>> extends ConduitConnection.Inventory<V>{

	private final Capability<T> cap;
	protected LazyOptional<T> inventory;
	private boolean resolved = false;

	public CapabilityInventory(final ConduitType<V> type, final Direction fromDir, final InventoryConnectionSettings settings, final Capability<T> cap) {
		super(type, fromDir, settings);
		this.cap = cap;
	}

	@Override
	public boolean resolve(){
		final BlockEntity be = this.owner.getLevel().getBlockEntity(this.owner.getBlockPos().relative(this.fromDir));
		if(be == null) {
			this.inventory = LazyOptional.empty();
			return false;
		}
		this.inventory = be.getCapability(this.cap, this.fromDir);
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
	public void accept(final V quantity) {
		this.inventory.ifPresent(quantity::insertInto);
	}

	public static class Item extends CapabilityInventory<IItemHandler, TransferrableQuantity.ItemQuantity>{

		public static Codec<Item> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Direction.CODEC.fieldOf("fromDir").forGetter(x -> x.fromDir),
				InventoryConnectionSettings.CODEC.fieldOf("settings").forGetter(Inventory::getSettings))
				.apply(instance, Item::new));

		public Item(final Direction fromDir, final InventoryConnectionSettings settings) {
			super(ModConduits.ITEM.get(), fromDir, settings, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);

		}

		@Override
		public List<ItemQuantity> nextAvailable() {
			return this.inventory.map(handler -> {
				final int slots = handler.getSlots();
				if(slots == 0)
					return Collections.<ItemQuantity>emptyList();
				final List<ItemQuantity> items = new ArrayList<>(slots);
				for(int i = 0; i < slots; i++) {
					final ItemStack stack = handler.getStackInSlot(i);
					if(!stack.isEmpty())
						items.add(new ItemQuantity(handler, i));
				}
				return items;
			}).orElseGet(Collections::emptyList);
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
			super(ModConduits.ENERGY.get(), fromDir, settings, CapabilityEnergy.ENERGY);
		}

		@Override
		public List<EnergyQuantity> nextAvailable() {
			return this.inventory.filter(storage -> storage.getEnergyStored() > 0)
					.map(handler -> ImmutableList.of(new EnergyQuantity(handler))).orElseGet(ImmutableList::of);
		}

	}

	public static class Fluid extends CapabilityInventory<IFluidHandler, TransferrableQuantity.FluidQuantity>{

		public static Codec<Fluid> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Direction.CODEC.fieldOf("fromDir").forGetter(x -> x.fromDir),
				InventoryConnectionSettings.CODEC.fieldOf("settings").forGetter(Inventory::getSettings))
				.apply(instance, Fluid::new));

		public Fluid(final Direction fromDir, final InventoryConnectionSettings settings) {
			super(ModConduits.FLUID.get(), fromDir, settings, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
		}

		@Override
		public List<FluidQuantity> nextAvailable() {
			return this.inventory.filter(handler -> !FluidHandlerUtil.isEmpty(handler))
					.map(handler -> ImmutableList.of(new FluidQuantity(handler))).orElseGet(ImmutableList::of);
		}

	}

}
