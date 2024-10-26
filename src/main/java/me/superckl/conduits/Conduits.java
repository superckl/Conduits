package me.superckl.conduits;

import java.util.Optional;

import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLServiceProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
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
import org.jetbrains.annotations.NotNull;

@Mod(Conduits.MOD_ID)
public class Conduits {

	public static final String MOD_ID = "conduits";
	public static CreativeModeTab CONDUIT_TAB;

	public static final Logger LOG = LogManager.getFormatterLogger(Conduits.MOD_ID);

	public Conduits(IEventBus modBus) {


		ModAttachments.ATTACHMENT_TYPES.register(modBus);
		ModTabs.TABS.register(modBus);
		ModBlocks.BLOCKS.register(modBus);
		ModBlocks.ENTITIES.register(modBus);
		ModItems.ITEMS.register(modBus);
		ModContainers.MENU_TYPES.register(modBus);
		ModConduits.TYPES.register(modBus);

		Conduits.CONDUIT_TAB = new CreativeModeTab(CreativeModeTab.builder().title(Component.translatable("itemGroup.conduits")).icon(() -> ModItems.CONDUITS.get(ModConduits.ENERGY.getId()).get(ConduitTier.MIDDLE).get().getDefaultInstance())) {
		};

		NeoForge.EVENT_BUS.addListener(this::registerCommands);

	}

	private void registerCommands(final RegisterCommandsEvent e) {
		e.getDispatcher().register(Commands.literal("conduits").requires(cs -> cs.hasPermission(2))
				.then(Commands.literal("network").executes(ViewNetworkCommand::execute)));
	}

}
