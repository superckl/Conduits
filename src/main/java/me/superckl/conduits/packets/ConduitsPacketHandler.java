package me.superckl.conduits.packets;

import me.superckl.conduits.Conduits;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ConduitsPacketHandler {

	private static final String PROTOCOL_VERSION = "1";

	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(Conduits.MOD_ID, "conduits"),
			() -> ConduitsPacketHandler.PROTOCOL_VERSION, ConduitsPacketHandler.PROTOCOL_VERSION::equals,
			ConduitsPacketHandler.PROTOCOL_VERSION::equals);

}
