package me.superckl.conduits.conduit.network;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import me.superckl.conduits.Conduits;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = Conduits.MOD_ID)
public class NetworkTicker {

	public static final Capability<NetworkTicker> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
	private static final ResourceLocation CAP_LOC = new ResourceLocation(Conduits.MOD_ID, "network_ticker");

	private final List<WeakReference<ConduitNetwork>> networks = new ArrayList<>();
	private final List<ConduitNetwork> toAdd = new ArrayList<>();
	private final List<ConduitNetwork> toRemove = new ArrayList<>();

	public void tick() {
		this.toAdd.forEach(network -> this.networks.add(new WeakReference<>(network)));
		this.toAdd.clear();
		final Iterator<WeakReference<ConduitNetwork>> it = this.networks.iterator();
		while(it.hasNext()) {
			final ConduitNetwork ref = it.next().get();
			if(ref == null || this.toRemove.contains(ref))
				it.remove();
			else
				ref.tick();
		}
		this.toRemove.clear();
	}

	public void remove(final ConduitNetwork network) {
		this.toRemove.add(network);
	}

	public void add(final ConduitNetwork network) {
		this.toAdd.add(network);
	}

	@SubscribeEvent
	public static void onLevelTick(final WorldTickEvent e) {
		if(e.side == LogicalSide.CLIENT || e.phase == Phase.START)
			return;
		e.world.getCapability(NetworkTicker.CAPABILITY).ifPresent(NetworkTicker::tick);
	}

	@SuppressWarnings("resource")
	@SubscribeEvent
	public static void attachCaps(final AttachCapabilitiesEvent<Level> e) {
		if(e.getObject().isClientSide)
			return;
		e.addCapability(NetworkTicker.CAP_LOC, new Provider());
	}

	public static class Provider implements ICapabilityProvider{

		private final LazyOptional<NetworkTicker> ticker = LazyOptional.of(NetworkTicker::new);

		@Override
		public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side) {
			if(cap == NetworkTicker.CAPABILITY)
				return this.ticker.cast();
			return LazyOptional.empty();
		}

	}

}
