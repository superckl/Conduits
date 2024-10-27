package me.superckl.conduits.packets;

import me.superckl.conduits.Conduits;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Conduits.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ConduitsPacketHandler {

    private static final String PROTOCOL_VERSION = "1";

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent e) {
        final PayloadRegistrar registrar = e.registrar(PROTOCOL_VERSION);
        registrar.playToServer(SyncConduitSettingPacket.Data.TYPE, SyncConduitSettingPacket.Data.STREAM_CODEC, SyncConduitSettingPacket::handleServer);
    }

}
