package me.superckl.conduits;

import me.superckl.conduits.conduit.network.NetworkTicker;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Conduits.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<NetworkTicker>> NETWORK_TICKER = ATTACHMENT_TYPES.register("network_ticker", () -> AttachmentType.builder(NetworkTicker::new).build());

}
