package me.superckl.conduits;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Conduits.MOD_ID)
public class Conduits {

	public static final String MOD_ID = "conduits";
	public static CreativeModeTab CONDUIT_TAB;

	public static final Logger LOG = LogManager.getFormatterLogger(Conduits.MOD_ID);

	public Conduits() {

		final var bus = FMLJavaModLoadingContext.get().getModEventBus();

		ModBlocks.BLOCKS.register(bus);
		ModBlocks.ENTITIES.register(bus);
		ModItems.ITEMS.register(bus);

		Conduits.CONDUIT_TAB = new CreativeModeTab(Conduits.MOD_ID) {
			@Override
			public ItemStack makeIcon() {
				return ModItems.CONDUITS.get(ConduitType.ENERGY).get(ConduitTier.MIDDLE).get().getDefaultInstance();
			}
		};

	}

}
