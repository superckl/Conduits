package me.superckl.conduits.data;

import me.superckl.conduits.Conduits;
import me.superckl.conduits.data.client.ConduitsBlockStateProvider;
import me.superckl.conduits.data.client.ConduitsItemModelProvider;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@EventBusSubscriber(modid = Conduits.MOD_ID, bus = Bus.MOD)
public final class ConduitsDataGen {

	private ConduitsDataGen() {}

	@SubscribeEvent
	public static void gatherData(final GatherDataEvent e) {
		final var gen = e.getGenerator();
		final var fileHelper = e.getExistingFileHelper();
		if(e.includeClient()) {
			gen.addProvider(new ConduitsItemModelProvider(gen, fileHelper));
			gen.addProvider(new ConduitsBlockStateProvider(gen, fileHelper));
		}
		if(e.includeServer()) {

		}
	}

}
