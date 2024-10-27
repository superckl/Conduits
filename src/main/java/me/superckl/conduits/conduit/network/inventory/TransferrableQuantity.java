package me.superckl.conduits.conduit.network.inventory;

import lombok.RequiredArgsConstructor;
import me.superckl.conduits.util.FluidHandlerUtil;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.Collection;

public abstract class TransferrableQuantity {

    public abstract boolean isConsumed();

    @RequiredArgsConstructor
    public static abstract class CapabilityQuantity<T> extends TransferrableQuantity {

        protected final T handler;

        public abstract boolean insertInto(T other);

    }

    public static class ItemQuantity extends CapabilityQuantity<IItemHandler> {

        private final int slot;

        public ItemQuantity(final IItemHandler handler, final int slot) {
            super(handler);
            this.slot = slot;
        }

        @Override
        public boolean isConsumed() {
            return this.handler.getStackInSlot(this.slot).isEmpty();
        }

        @Override
        public boolean insertInto(final IItemHandler other) {
            final ItemStack stack = this.handler.getStackInSlot(this.slot);
            final ItemStack remainder = ItemHandlerHelper.insertItem(other, stack, true);
            if (remainder == stack || remainder.is(stack.getItem()) && remainder.getCount() == stack.getCount())
                return false;
            final int inserted = stack.getCount() - remainder.getCount();
            final ItemStack extracted = this.handler.extractItem(this.slot, inserted, false);
            ItemHandlerHelper.insertItem(other, extracted, false);
            return true;
        }

    }

    public static class FluidQuantity extends CapabilityQuantity<IFluidHandler> {

        public FluidQuantity(final IFluidHandler handler) {
            super(handler);
        }

        @Override
        public boolean isConsumed() {
            return FluidHandlerUtil.isEmpty(this.handler);
        }

        @Override
        public boolean insertInto(final IFluidHandler other) {
            final Collection<FluidStack> fluids = FluidHandlerUtil.availableFluid(this.handler);
            if (fluids.isEmpty())
                return false;
            boolean modified = false;
            for (final FluidStack stack : fluids) {
                final int inserted = other.fill(stack, IFluidHandler.FluidAction.SIMULATE);
                final FluidStack extracted = this.handler.drain(new FluidStack(stack.getFluid(), inserted), IFluidHandler.FluidAction.EXECUTE);
                other.fill(extracted, IFluidHandler.FluidAction.EXECUTE);
                modified |= extracted.getAmount() > 0;
            }
            return modified;
        }

    }

    public static class EnergyQuantity extends CapabilityQuantity<IEnergyStorage> {

        public EnergyQuantity(final IEnergyStorage handler) {
            super(handler);
        }

        @Override
        public boolean insertInto(final IEnergyStorage other) {
            final int energy = this.handler.getEnergyStored();
            final int inserted = other.receiveEnergy(energy, true);
            final int extracted = this.handler.extractEnergy(inserted, false);
            other.receiveEnergy(extracted, false);
            return extracted != 0;
        }

        @Override
        public boolean isConsumed() {
            return this.handler.getEnergyStored() <= 0;
        }

    }

}
