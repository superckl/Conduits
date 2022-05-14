package me.superckl.conduits.conduit.connection;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;

public abstract class ConnectionHelper {

	public abstract boolean canConnect(Direction dir, BlockEntity be);

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	public static class CapabilityConnectionHelper extends ConnectionHelper{

		private final Capability<?> cap;

		@Override
		public boolean canConnect(final Direction dir, final BlockEntity be) {
			return be.getCapability(this.cap, dir).isPresent();
		}

		public static CapabilityConnectionHelper forCapability(final Capability<?> capability) {
			return new CapabilityConnectionHelper(capability);
		}

	}
}
