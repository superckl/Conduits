package me.superckl.conduits.conduit.network;

import me.superckl.conduits.Conduits;
import me.superckl.conduits.ModAttachments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@EventBusSubscriber(modid = Conduits.MOD_ID)
public class NetworkTicker {

    private final List<WeakReference<ConduitNetwork<?>>> networks = new ArrayList<>();
    private final List<ConduitNetwork<?>> toAdd = new ArrayList<>();
    private final List<ConduitNetwork<?>> toRemove = new ArrayList<>();

    public void tick() {
        this.toAdd.forEach(network -> this.networks.add(new WeakReference<>(network)));
        this.toAdd.clear();
        final Iterator<WeakReference<ConduitNetwork<?>>> it = this.networks.iterator();
        while (it.hasNext()) {
            final ConduitNetwork<?> ref = it.next().get();
            if (ref == null || this.toRemove.contains(ref))
                it.remove();
            else
                ref.tick();
        }
        this.toRemove.clear();
    }

    public void remove(final ConduitNetwork<?> network) {
        this.toRemove.add(network);
    }

    public void add(final ConduitNetwork<?> network) {
        this.toAdd.add(network);
    }

    @SubscribeEvent
    public static void onLevelTick(final LevelTickEvent.Post e) {
        if (e.getLevel().isClientSide())
            return;
        e.getLevel().getData(ModAttachments.NETWORK_TICKER.get()).tick();
    }

}
