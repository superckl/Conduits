package me.superckl.conduits.util;

import java.util.Collection;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class FluidHandlerUtil {

	public static Collection<FluidStack> availableFluid(final IFluidHandler handler){
		final Map<Fluid, FluidStack> stacks = new Object2ObjectOpenHashMap<>();
		final int tanks = handler.getTanks();
		for (int i = 0; i < tanks; i++) {
			final FluidStack stack = handler.getFluidInTank(i);
			if(stack.isEmpty())
				continue;
			stacks.computeIfAbsent(stack.getFluid(), fluid -> new FluidStack(fluid, 0)).grow(stack.getAmount());
		}
		return stacks.values();
	}

	public static boolean isEmpty(final IFluidHandler handler) {
		final int tanks = handler.getTanks();
		boolean empty = true;
		for(int i = 0; i < tanks; i++)
			empty &= handler.getFluidInTank(i).isEmpty();
		return empty;
	}

}
