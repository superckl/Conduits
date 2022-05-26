package me.superckl.conduits.conduit.network.inventory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import me.superckl.conduits.ModConduits;
import me.superckl.conduits.conduit.ConduitType;
import me.superckl.conduits.conduit.connection.ConduitConnection;
import me.superckl.conduits.conduit.connection.InventoryConnectionSettings;
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

	public CapabilityInventory(final ConduitType type, final Direction fromDir, final InventoryConnectionSettings settings, final Capability<T> cap) {
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

	public static class Item extends CapabilityInventory<IItemHandler>{

		public static Codec<Item> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Direction.CODEC.fieldOf("fromDir").forGetter(x -> x.fromDir),
				InventoryConnectionSettings.CODEC.fieldOf("settings").forGetter(Inventory::getSettings))
				.apply(instance, Item::new));

		public Item(final Direction fromDir, final InventoryConnectionSettings settings) {
			super(ModConduits.ITEM.get(), fromDir, settings, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
		}

	}

	public static class Energy extends CapabilityInventory<IEnergyStorage>{

		public static Codec<Energy> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Direction.CODEC.fieldOf("fromDir").forGetter(x -> x.fromDir),
				InventoryConnectionSettings.CODEC.fieldOf("settings").forGetter(Inventory::getSettings))
				.apply(instance, Energy::new));

		public Energy(final Direction fromDir, final InventoryConnectionSettings settings) {
			super(ModConduits.ENERGY.get(), fromDir, settings, CapabilityEnergy.ENERGY);
		}

	}

	public static class Fluid extends CapabilityInventory<IFluidHandler>{

		public static Codec<Fluid> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Direction.CODEC.fieldOf("fromDir").forGetter(x -> x.fromDir),
				InventoryConnectionSettings.CODEC.fieldOf("settings").forGetter(Inventory::getSettings))
				.apply(instance, Fluid::new));

		public Fluid(final Direction fromDir, final InventoryConnectionSettings settings) {
			super(ModConduits.FLUID.get(), fromDir, settings, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
		}

	}

}
