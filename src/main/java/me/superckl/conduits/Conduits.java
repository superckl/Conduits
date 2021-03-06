package me.superckl.conduits;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.superckl.conduits.conduit.ConduitTier;
import me.superckl.conduits.conduit.network.NetworkTicker;
import me.superckl.conduits.packets.ConduitsPacketHandler;
import me.superckl.conduits.packets.SyncConduitSettingPacket;
import me.superckl.conduits.server.command.ViewNetworkCommand;
import net.minecraft.commands.Commands;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkDirection;

@Mod(Conduits.MOD_ID)
public class Conduits {

	public static final String MOD_ID = "conduits";
	public static CreativeModeTab CONDUIT_TAB;

	public static final Logger LOG = LogManager.getFormatterLogger(Conduits.MOD_ID);

	public Conduits() {

		final var bus = FMLJavaModLoadingContext.get().getModEventBus();

		bus.addListener(this::registerCaps);

		ModBlocks.BLOCKS.register(bus);
		ModBlocks.ENTITIES.register(bus);
		ModItems.ITEMS.register(bus);
		ModContainers.MENU_TYPES.register(bus);
		ModConduits.TYPES.register(bus);

		Conduits.CONDUIT_TAB = new CreativeModeTab(Conduits.MOD_ID) {
			@Override
			public ItemStack makeIcon() {
				return ModItems.CONDUITS.get(ModConduits.ENERGY.getId()).get(ConduitTier.MIDDLE).get().getDefaultInstance();
			}
		};

		MinecraftForge.EVENT_BUS.addListener(this::registerCommands);

		ConduitsPacketHandler.INSTANCE.registerMessage(0, SyncConduitSettingPacket.class, SyncConduitSettingPacket::write,
				SyncConduitSettingPacket::new, SyncConduitSettingPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));

	}

	private void registerCaps(final RegisterCapabilitiesEvent e) {
		e.register(NetworkTicker.class);
	}

	private void registerCommands(final RegisterCommandsEvent e) {
		e.getDispatcher().register(Commands.literal("conduits").requires(cs -> cs.hasPermission(2))
				.then(Commands.literal("network").executes(ViewNetworkCommand::execute)));
	}

}
