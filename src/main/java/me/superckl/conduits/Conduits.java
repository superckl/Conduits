package me.superckl.conduits;

import me.superckl.conduits.server.command.ViewNetworkCommand;
import net.minecraft.commands.Commands;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Conduits.MOD_ID)
public class Conduits {

    public static final String MOD_ID = "conduits";

    public static final Logger LOG = LogManager.getFormatterLogger(Conduits.MOD_ID);

    public Conduits(IEventBus modBus) {

        ModAttachments.ATTACHMENT_TYPES.register(modBus);
        ModTabs.TABS.register(modBus);
        ModBlocks.BLOCKS.register(modBus);
        ModBlocks.ENTITIES.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModContainers.MENU_TYPES.register(modBus);
        ModConduits.TYPES.register(modBus);

        NeoForge.EVENT_BUS.addListener(this::registerCommands);

    }

    private void registerCommands(final RegisterCommandsEvent e) {
        e.getDispatcher().register(Commands.literal("conduits").requires(cs -> cs.hasPermission(2))
                .then(Commands.literal("network").executes(ViewNetworkCommand::execute)));
    }

}
